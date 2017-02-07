package com.icfolson.aem.monitoring.database.writer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringEvent;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventTypeRecord;
import com.icfolson.aem.monitoring.database.model.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.repository.impl.EventRepositoryImpl;
import com.icfolson.aem.monitoring.database.util.NameUtil;
import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EventsDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(EventRepositoryImpl.class);

    private final UUID systemId;
    private final ConnectionProvider connectionProvider;
    private final BiMap<String, Short> eventTypeMap = HashBiMap.create();

    public EventsDatabase(final UUID systemId, final ConnectionProvider connectionProvider) {
        this.systemId = systemId;
        this.connectionProvider = connectionProvider;
        try {
            initEvents();
        } catch (MonitoringDBException e) {
            LOG.error("Error initializing events", e);
        }
    }

    public BiMap<String, Short> getEventTypeMap() {
        return HashBiMap.create(eventTypeMap);
    }

    public void writeEvent(final MonitoringEvent event) {
        final String joinedName = NameUtil.toStorageFormat(event.getType());
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            Short type = eventTypeMap.get(joinedName);
            if (type == null) {
                type = initEvent(joinedName);
            }
            final EventRecord record = context.newRecord(Tables.EVENT);
            record.setSystemId(systemId);
            record.setEventTypeId(type);
            record.setTime(event.getTimestamp());
            record.insert();
            final Long eventId = record.getEventId();
            for (final Map.Entry<String, Object> entry : event.getProperties().entrySet()) {
                final Object value = entry.getValue();
                if (value != null) {
                    final EventPropertyRecord propertyRecord = context.newRecord(Tables.EVENT_PROPERTY);
                    propertyRecord.setEventId(eventId);
                    propertyRecord.setName(entry.getKey());
                    if (value instanceof Number) {
                        propertyRecord.setRealvalue(((Number) value).floatValue());
                    } else {
                        propertyRecord.setValue(String.valueOf(value));
                    }
                    propertyRecord.insert();
                }
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing event data", e);
        }
    }

    public List<MonitoringEvent> getEvents(final Long since, final Integer limit) {
        final List<MonitoringEvent> out = new ArrayList<>();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Result<Record6<Long, Short, Long, String, Float, String>> results = context
                .select(Tables.EVENT.EVENT_ID, Tables.EVENT.EVENT_TYPE_ID, Tables.EVENT.TIME,
                    Tables.EVENT_PROPERTY.NAME, Tables.EVENT_PROPERTY.REALVALUE, Tables.EVENT_PROPERTY.VALUE)
                .from(Tables.EVENT).join(Tables.EVENT_PROPERTY)
                .on(Tables.EVENT.EVENT_ID.eq(Tables.EVENT_PROPERTY.EVENT_ID))
                .where(Tables.EVENT.EVENT_ID.in(
                    context.select(Tables.EVENT.EVENT_ID).from(Tables.EVENT)
                        .where(Tables.EVENT.TIME.greaterOrEqual(since)).limit(limit)
                ))
                .orderBy(Tables.EVENT.TIME)
                .fetch();
            Map<Long, MonitoringEvent> map = new LinkedHashMap<>();
            final BiMap<Short, String> eventTypes = getEventTypeMap().inverse();
            for (final Record6<Long, Short, Long, String, Float, String> result : results) {
                long eventId = result.value1();
                MonitoringEvent event = map.get(eventId);
                if (event == null) {
                    final Short eventTypeId = result.value2();
                    final String eventType = eventTypes.get(eventTypeId);
                    final Long timestamp = result.value3();
                    event = new DefaultMonitoringEvent(NameUtil.toName(eventType), timestamp);
                    map.put(eventId, event);
                }
                Object value = result.value5();
                if (value == null) {
                    value = result.value6();
                }
                event.setProperty(result.value4(), value);
            }
            return new ArrayList<>(map.values());
        } catch (MonitoringDBException e) {
            LOG.error("Error loading events", e);
        }
        return out;
    }

    public long getLatestEventTimestamp() {
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Long time = context.select(Tables.EVENT.TIME.max()).from(Tables.EVENT)
                .where(Tables.EVENT.SYSTEM_ID.eq(systemId)).fetchOne(0, Long.class);
            if (time != null) {
                return time;
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error getting latest event timestamp", e);
        }
        return -1;
    }

    private synchronized short initEvent(final String joinedName) throws MonitoringDBException {
        initEvents();
        Short eventTypeId = eventTypeMap.get(joinedName);
        if (eventTypeId != null) {
            return eventTypeId;
        }
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final EventTypeRecord eventTypeRecord = context.newRecord(Tables.EVENT_TYPE);
            eventTypeRecord.setEventName(joinedName);
            eventTypeRecord.store();
            eventTypeId = eventTypeRecord.getEventTypeId();
            eventTypeMap.put(joinedName, eventTypeId);
            return eventTypeId;
        } catch (MonitoringDBException e) {
            throw new MonitoringDBException("Error writing new event type", e);
        }
    }

    private void initEvents() throws MonitoringDBException {
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Result<EventTypeRecord> records = context.selectFrom(Tables.EVENT_TYPE).fetch();
            eventTypeMap.clear();
            for (final EventTypeRecord record : records) {
                eventTypeMap.put(record.getEventName(), record.getEventTypeId());
            }
        } catch (MonitoringDBException e) {
            throw new MonitoringDBException("Error loading event types", e);
        }
    }

    private ConnectionWrapper getConnection() throws MonitoringDBException {
        return connectionProvider.getConnection();
    }
}
