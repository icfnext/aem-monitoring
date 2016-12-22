package com.icfolson.aem.monitoring.database;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.util.TimeUtil;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.System;
import com.icfolson.aem.monitoring.database.generated.tables.SystemProperty;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterValueRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricValueRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class MonitoringDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringDatabase.class);

    private final SystemInfo systemInfo;
    private final ZoneId systemZone;
    private final ConnectionProvider connectionProvider;
    private final HierarchyCache metricIds = new HierarchyCache();
    private final HierarchyCache counterIds = new HierarchyCache();

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
            final long id = initMetric(metric.getName());
            record.setMetricId(id);
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
            final long id = initCounter(counter.getName());
            record.setCounterId(id);
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
            final Result<MetricRecord> metricRecords = context
                .selectFrom(Tables.METRIC)
                .orderBy(Tables.METRIC.METRIC_ID)
                .fetch();
            for (final MetricRecord metricRecord : metricRecords) {
                final Long parentId = metricRecord.getParentMetricId();
                final String name = metricRecord.getMetricName();
                final Long id = metricRecord.getMetricId();
                metricIds.storeId(parentId, name, id);
            }
            final Result<CounterRecord> counterRecords = context
                .selectFrom(Tables.COUNTER)
                .orderBy(Tables.COUNTER.COUNTER_ID)
                .fetch();
            for (final CounterRecord counterRecord : counterRecords) {
                final Long parentId = counterRecord.getParentCounterId();
                final String name = counterRecord.getCounterName();
                final Long id = counterRecord.getCounterId();
                counterIds.storeId(parentId, name, id);
            }

        } catch (MonitoringDBException e) {
            LOG.error("Error writing system data", e);
        }
    }

    private long initMetric(final String[] names) throws MonitoringDBException {
        final Long id = metricIds.getId(names);
        if (id != null) {
            return id;
        } else {
            try (DSLContext context = getContext()) {
                HierarchyCache.HierarchyNode prev = null;
                HierarchyCache.HierarchyNode node = metricIds.root;
                for (int i = 0; i < names.length; i++) {
                    final String name = names[i];
                    prev = node;
                    node = prev.children.get(name);
                    if (node == null) {
                        final MetricRecord metricRecord = context.newRecord(Tables.METRIC);
                        if (prev != metricIds.root) {
                            metricRecord.setParentMetricId(prev.id);
                        }
                        metricRecord.setMetricName(name);
                        metricRecord.insert();
                        metricRecord.refresh();
                        node = new HierarchyCache.HierarchyNode(metricRecord.getMetricId());
                        prev.children.put(name, node);
                    }
                }
                return node.id;
            } catch (MonitoringDBException e) {
                throw new MonitoringDBException(e);
            }
        }
    }

    private long initCounter(final String[] names) throws MonitoringDBException {
        final Long id = counterIds.getId(names);
        if (id != null) {
            return id;
        } else {
            try (DSLContext context = getContext()) {
                HierarchyCache.HierarchyNode prev = null;
                HierarchyCache.HierarchyNode node = counterIds.root;
                for (int i = 0; i < names.length; i++) {
                    final String name = names[i];
                    prev = node;
                    node = prev.children.get(name);
                    if (node == null) {
                        final CounterRecord counterRecord = context.newRecord(Tables.COUNTER);
                        if (prev != metricIds.root) {
                            counterRecord.setParentCounterId(prev.id);
                        }
                        counterRecord.setCounterName(name);
                        counterRecord.insert();
                        counterRecord.refresh();
                        node = new HierarchyCache.HierarchyNode(counterRecord.getCounterId());
                        prev.children.put(name, node);
                    }
                }
                return node.id;
            } catch (MonitoringDBException e) {
                throw new MonitoringDBException(e);
            }
        }
    }

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.H2);
    }

    private static class HierarchyCache {

        private static class HierarchyNode {

            private final long id;
            private final Map<String, HierarchyNode> children = new HashMap<>();

            private HierarchyNode(final long id) {
                this.id = id;
            }
        }

        private final HierarchyNode root = new HierarchyNode(-1);
        private final Map<Long, HierarchyNode> byId = new HashMap<>();

        public Long getId(final String[] names) {
            HierarchyNode pointer = root;
            for (final String name : names) {
                pointer = pointer.children.get(name);
                if (pointer == null) {
                    return null;
                }
            }
            return pointer.id;
        }

        public void storeId(final String[] names, final long id) {
            HierarchyNode pointer = root;
            for (int i = 0; i < names.length - 1; i++) {
                pointer = pointer.children.get(names[i]);
                if (pointer == null) {
                    throw new IllegalStateException("Attempted to set ID for node without cached parent");
                }
            }
            final HierarchyNode node = new HierarchyNode(id);
            pointer.children.put(names[names.length - 1], node);
            byId.put(id, node);
        }

        public void storeId(final Long parentId, final String name, final long id) {
            HierarchyNode parent;
            if (parentId != null) {
                parent = byId.get(parentId);
                if (parent == null) {
                    throw new IllegalStateException("Attempted to set ID for node without cached parent");
                }
            } else {
                parent = root;
            }
            final HierarchyNode node = new HierarchyNode(id);
            parent.children.put(name, node);
            byId.put(id, node);
        }

    }
}
