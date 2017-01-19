package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.util.TimeUtil;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.SystemInfo;
import com.icfolson.aem.monitoring.database.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventTypeRecord;
import com.icfolson.aem.monitoring.database.repository.EventRepository;
import com.icfolson.aem.monitoring.database.util.NameUtil;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Component(immediate = true)
public class EventRepositoryImpl implements EventRepository {

    private static final Logger LOG = LoggerFactory.getLogger(EventRepositoryImpl.class);

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private SystemInfoProvider systemInfoProvider;
    private SystemInfo systemInfo;

    private final ZoneId systemZone = ZoneId.systemDefault();
    private final Map<String, Short> eventTypeMap = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Map<String, Short> getEventTypeMap() {
        return Collections.unmodifiableMap(eventTypeMap);
    }

    @Override
    public void writeEvent(final MonitoringEvent event) {
        final String joinedName = NameUtil.toStorageFormat(event.getType());
        try (DSLContext context = getContext()) {
            Short type = eventTypeMap.get(joinedName);
            if (type == null) {
                type = initEvent(joinedName);
            }
            final EventRecord record = context.newRecord(Tables.EVENT);
            record.setSystemId(systemInfo.getSystemId());
            record.setEventTypeId(type);
            record.setTime(TimeUtil.toEpochMs(event.getTimestamp(), systemZone));
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

    @Activate
    @Modified
    protected final void activate(final Map<String, Object> props) {
        systemInfo = systemInfoProvider.getSystemInfo();
        try {
            initEvents();
        } catch (MonitoringDBException e) {
            LOG.error("Failed to load initial event type IDs", e);
        }
    }
}
