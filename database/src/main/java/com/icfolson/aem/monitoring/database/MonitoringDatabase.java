package com.icfolson.aem.monitoring.database;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.util.TimeUtil;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.System;
import com.icfolson.aem.monitoring.database.generated.tables.SystemProperty;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterValueRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricValueRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.Map;

public class MonitoringDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringDatabase.class);

    private final SystemInfo systemInfo;
    private final ZoneId systemZone;
    private final ConnectionProvider connectionProvider;

    public MonitoringDatabase(final SystemInfo systemInfo, final ConnectionProvider connectionProvider) {
        this.systemInfo = systemInfo;
        this.systemZone = ZoneId.systemDefault(); // TODO
        this.connectionProvider = connectionProvider;
        initSystem();
    }

    public void writeEvent(final MonitoringEvent event) {
        try (DSLContext context = getContext()) {
            final EventRecord record = context.newRecord(Tables.EVENT);
            record.setSystemId(systemInfo.getSystemId());
            record.setType(event.getType());
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

    public void writeMetric(final MonitoringMetric metric) {
        try (DSLContext context = getContext()) {
            final MetricValueRecord record = context.newRecord(Tables.METRIC_VALUE);
            record.setSystemId(systemInfo.getSystemId());
            record.setTime(metric.getTimestamp().atZone(systemZone).toInstant().toEpochMilli());
            record.setMetricName(metric.getName());
            record.setMetricValue(metric.getValue());
            record.insert();
        } catch (MonitoringDBException e) {
            LOG.error("Error writing metric data", e);
        }
    }

    public void writeCounter(final MonitoringCounter counter) {
        try (DSLContext context = getContext()) {
            final CounterValueRecord record = context.newRecord(Tables.COUNTER_VALUE);
            record.setSystemId(systemInfo.getSystemId());
            record.setTime(counter.getTimestamp().atZone(systemZone).toInstant().toEpochMilli());
            record.setMetricName(counter.getName());
            record.setIncrementValue(counter.getIncrement());
            record.insert();
        } catch (MonitoringDBException e) {
            LOG.error("Error writing counter data", e);
        }
    }

    private void initSystem() {
        try (DSLContext context = getContext()) {
            SystemRecord systemRecord = context
                .selectFrom(Tables.SYSTEM)
                .where(System.SYSTEM.SYSTEM_ID.eq(systemInfo.getSystemId()))
                .fetchAny();
            if (systemRecord == null) {
                systemRecord = context.newRecord(Tables.SYSTEM);
                systemRecord.setSystemId(systemInfo.getSystemId());
                systemRecord.insert();
            }
            context.deleteFrom(SystemProperty.SYSTEM_PROPERTY)
                .where(SystemProperty.SYSTEM_PROPERTY.SYSTEM_ID.eq(systemInfo.getSystemId()))
                .execute();
            for (final String name : systemInfo.getPropertyNames()) {
                SystemPropertyRecord propertyRecord = context
                    .selectFrom(Tables.SYSTEM_PROPERTY)
                    .where(
                        SystemProperty.SYSTEM_PROPERTY.SYSTEM_ID.eq(systemInfo.getSystemId())
                            .and(SystemProperty.SYSTEM_PROPERTY.NAME.eq(name))
                    ).fetchAny();
                if (propertyRecord == null) {
                    propertyRecord = context.newRecord(Tables.SYSTEM_PROPERTY);
                    propertyRecord.setSystemId(systemInfo.getSystemId());
                    propertyRecord.setName(name);
                    propertyRecord.setValue(systemInfo.getPropertyValue(name));
                    propertyRecord.insert();
                }
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing system data", e);
        }
    }

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.H2);
    }
}
