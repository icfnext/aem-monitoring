package com.icfolson.aem.monitoring.database.writer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventTypeRecord;
import com.icfolson.aem.monitoring.database.repository.impl.EventRepositoryImpl;
import com.icfolson.aem.monitoring.database.util.NameUtil;
import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventsDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(EventRepositoryImpl.class);

    private final UUID systemId;
    private final ConnectionProvider connectionProvider;
    private final BiMap<String, Short> eventTypeMap = HashBiMap.create();

    public EventsDatabase(final UUID systemId, final ConnectionProvider connectionProvider) {
        this.systemId = systemId;
        this.connectionProvider = connectionProvider;
    }

    public BiMap<String, Short> getEventTypeMap() {
        return HashBiMap.create(eventTypeMap);
    }

    public void writeEvent(final MonitoringEvent event) {
        final String joinedName = NameUtil.toStorageFormat(event.getType());
        try (DSLContext context = getContext()) {
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
        try (final DSLContext context = getContext()) {
            final Result<Record6<Long, Short, Long, String, Float, String>> results = context
                .select(Tables.EVENT.EVENT_ID, Tables.EVENT.EVENT_TYPE_ID, Tables.EVENT.TIME,
                    Tables.EVENT_PROPERTY.NAME, Tables.EVENT_PROPERTY.REALVALUE, Tables.EVENT_PROPERTY.VALUE)
                .from(Tables.EVENT).join(Tables.EVENT_PROPERTY)
                .on(Tables.EVENT.EVENT_ID.eq(Tables.EVENT_PROPERTY.EVENT_ID))
                .where(Tables.EVENT.EVENT_ID.in(
                    context.select(Tables.EVENT.EVENT_ID).from(Tables.EVENT)
                        .where(Tables.EVENT.TIME.greaterOrEqual(since)).limit(limit)
                )).fetch();

        } catch (MonitoringDBException e) {
            LOG.error("Error loading events", e);
        }
        return out;
    }

    private synchronized short initEvent(final String joinedName) throws MonitoringDBException {
        initEvents();
        Short eventTypeId = eventTypeMap.get(joinedName);
        if (eventTypeId != null) {
            return eventTypeId;
        }
        try (DSLContext context = getContext()) {
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
        try (DSLContext context = getContext()) {
            final Result<EventTypeRecord> records = context.selectFrom(Tables.EVENT_TYPE).fetch();
            eventTypeMap.clear();
            for (final EventTypeRecord record : records) {
                eventTypeMap.put(record.getEventName(), record.getEventTypeId());
            }
        } catch (MonitoringDBException e) {
            throw new MonitoringDBException("Error loading event types", e);
        }
    }

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.H2);
    }
}
