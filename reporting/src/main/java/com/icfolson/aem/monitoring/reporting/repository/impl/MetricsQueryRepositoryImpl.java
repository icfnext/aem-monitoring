package com.icfolson.aem.monitoring.reporting.repository.impl;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import com.icfolson.aem.monitoring.reporting.repository.MetricsQueryRepository;
import com.icfolson.aem.monitoring.reporting.result.MetricsTimeSeries;
import com.icfolson.aem.monitoring.reporting.result.TimeSeries;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Result;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Component(immediate = true)
public class MetricsQueryRepositoryImpl implements MetricsQueryRepository {

    @Reference
    private ConnectionProvider connectionProvider;

    @Override
    public MetricsTimeSeries getMetricData(final short metricId, final TimeGrouper grouper)
        throws MonitoringDBException {

        final MetricsTimeSeries out = new MetricsTimeSeries();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Field<Long> bin = Tables.METRIC_VALUE.TIME.sub(grouper.getStartEpoch())
                .div(grouper.getBinLength()).floor();
            final Result<Record4<UUID, BigDecimal, Integer, Long>> records = context
                .select(Tables.METRIC_VALUE.SYSTEM_ID.cast(UUID.class),
                    Tables.METRIC_VALUE.METRIC_VALUE_.avg(), Tables.METRIC_VALUE.METRIC_VALUE_.count(), bin)
                .from(Tables.METRIC_VALUE)
                .where(Tables.METRIC_VALUE.TIME.greaterThan(grouper.getStartEpoch()))
                .and(Tables.METRIC_VALUE.TIME.lessThan(grouper.getEndEpoch()))
                .and(Tables.METRIC_VALUE.METRIC_ID.eq(metricId))
                .groupBy(Tables.METRIC_VALUE.SYSTEM_ID, bin, Tables.METRIC_VALUE.TIME)
                .orderBy(bin)
                .fetch();
            for (final Record4<UUID, BigDecimal, Integer, Long> record : records) {
                final UUID systemId = record.value1();
                TimeSeries timeSeries = out.getTimeSeries(systemId);
                if (timeSeries == null) {
                    TimeSeries ts = out.addFacet(systemId); // Temporary final variable for lambda
                    grouper.getPoints().forEach(value -> ts.newPoint(value, 0).build()); // zero out points
                    timeSeries = ts;
                }
                final long binNumber = record.value4();
                final long epoch = grouper.getBinStartTime(binNumber);
                final int binCount = record.value3();
                final TimeSeries.PointBuilder pointBuilder = timeSeries.newPoint(epoch, binCount);
                final Float average = record.value2().floatValue();
                pointBuilder.average(average).build();
            }
            return out;
        } catch (MonitoringDBException e) {
            throw new MonitoringDBException(e);
        }
    }

    private ConnectionWrapper getConnection() throws MonitoringDBException {
        return connectionProvider.getConnection();
    }
}
