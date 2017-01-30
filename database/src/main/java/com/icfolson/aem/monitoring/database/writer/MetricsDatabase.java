package com.icfolson.aem.monitoring.database.writer;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricValueRecord;
import com.icfolson.aem.monitoring.database.util.NameUtil;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MetricsDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsDatabase.class);

    private final UUID systemId;
    private final ConnectionProvider connectionProvider;
    private final Map<String, Short> metricIds = Collections.synchronizedMap(new HashMap<>());

    public MetricsDatabase(final UUID systemId, final ConnectionProvider connectionProvider) {
        this.systemId = systemId;
        this.connectionProvider = connectionProvider;
    }

    public Map<String, Short> getMetricTypes() {
        initMetrics();
        return new HashMap<>(metricIds);
    }

    public void writeMetric(final MonitoringMetric metric) {
        final String joinedName = NameUtil.toStorageFormat(metric.getName());
        try (DSLContext context = getContext()) {
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
        try (DSLContext context = getContext()) {
            final Result<MetricValueRecord> records = context.selectFrom(Tables.METRIC_VALUE)
                .where(Tables.METRIC_VALUE.TIME.greaterOrEqual(since)
                    .and(Tables.METRIC_VALUE.SYSTEM_ID.eq(systemId))).limit(limit).fetch();
            for (final MetricValueRecord record : records) {
                // TODO add CB interface to avoid successive copying
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing metric data", e);
        }
        return out;
    }

    private void initMetrics() {
        metricIds.clear();
        try (DSLContext context = getContext()) {
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
            try (DSLContext context = getContext()) {
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

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.H2);
    }

}
