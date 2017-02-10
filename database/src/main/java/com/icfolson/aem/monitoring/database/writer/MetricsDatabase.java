package com.icfolson.aem.monitoring.database.writer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringMetric;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricValueRecord;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import com.icfolson.aem.monitoring.core.util.NameUtil;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetricsDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsDatabase.class);

    private final UUID systemId;
    private final ConnectionProvider connectionProvider;
    private final BiMap<String, Short> metricIds = HashBiMap.create();

    public MetricsDatabase(final UUID systemId, final ConnectionProvider connectionProvider) {
        this.systemId = systemId;
        this.connectionProvider = connectionProvider;
    }

    public BiMap<String, Short> getMetricTypes() {
        initMetrics();
        return HashBiMap.create(metricIds);
    }

    public void writeMetric(final MonitoringMetric metric) {
        final String joinedName = NameUtil.toStorageFormat(metric.getName());
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final MetricValueRecord record = context.newRecord(Tables.METRIC_VALUE);
            record.setSystemId(systemId);
            record.setTime(metric.getTimestamp());
            Short id = metricIds.get(joinedName);
            if (id == null) {
                id = initMetric(joinedName);
            }
            record.setMetricId(id);
            record.setMetricValue(metric.getValue());
            record.insert();
        } catch (MonitoringDBException e) {
            LOG.error("Error writing metric data", e);
        }
    }

    public List<MonitoringMetric> getMetrics(final Long since, final Integer limit) {
        List<MonitoringMetric> out = new ArrayList<>();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final BiMap<Short, String> inverse = metricIds.inverse();
            final Result<MetricValueRecord> records = context.selectFrom(Tables.METRIC_VALUE)
                .where(Tables.METRIC_VALUE.TIME.greaterOrEqual(since)
                    .and(Tables.METRIC_VALUE.SYSTEM_ID.eq(systemId))).limit(limit).fetch();
            for (final MetricValueRecord record : records) {
                final Short id = record.getMetricId();
                final String name = inverse.get(id);
                final QualifiedName qualifiedName = NameUtil.toName(name);
                final Long time = record.getTime();
                final Float value = record.getMetricValue();
                out.add(new DefaultMonitoringMetric(qualifiedName, time, value));
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing metric data", e);
        }
        return out;
    }

    public long getLatestMetricTimestamp() {
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Long time = context.select(Tables.METRIC_VALUE.TIME.max()).from(Tables.METRIC_VALUE)
                .where(Tables.METRIC_VALUE.SYSTEM_ID.eq(systemId)).fetchOne(0, Long.class);
            if (time != null) {
                return time;
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error getting latest metric timestamp", e);
        }
        return -1;
    }

    private void initMetrics() {
        metricIds.clear();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Result<MetricRecord> metricRecords = context
                .selectFrom(Tables.METRIC)
                .orderBy(Tables.METRIC.METRIC_ID)
                .fetch();
            for (final MetricRecord metricRecord : metricRecords) {
                final String name = metricRecord.getMetricName();
                final Short id = metricRecord.getMetricId();
                metricIds.put(name, id);
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error fetching counter data", e);
        }
    }

    private synchronized short initMetric(final String joinedName) throws MonitoringDBException {
        Short id = metricIds.get(joinedName);
        if (id == null) {
            // Before attempting to add a new metric, make sure another system hasn't already done so
            initMetrics();
            id = metricIds.get(joinedName);
        }
        if (id != null) {
            return id;
        } else {
            try (ConnectionWrapper wrapper = getConnection()) {
                final DSLContext context = wrapper.getContext();
                final MetricRecord metricRecord = context.newRecord(Tables.METRIC);
                metricRecord.setMetricName(joinedName);
                metricRecord.insert();
                metricRecord.refresh();
                id = metricRecord.getMetricId();
                metricIds.put(joinedName, id);
                return id;
            } catch (MonitoringDBException e) {
                throw new MonitoringDBException(e);
            }
        }
    }

    private ConnectionWrapper getConnection() throws MonitoringDBException {
        return connectionProvider.getConnection();
    }

}
