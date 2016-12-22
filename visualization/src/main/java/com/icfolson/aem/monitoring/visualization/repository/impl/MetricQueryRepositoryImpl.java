package com.icfolson.aem.monitoring.visualization.repository.impl;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.visualization.repository.MetricQueryRepository;
import com.icfolson.aem.monitoring.visualization.result.TimeSeries;
import org.apache.felix.scr.annotations.Reference;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.math.BigDecimal;

public class MetricQueryRepositoryImpl implements MetricQueryRepository {

    @Reference
    private ConnectionProvider connectionProvider;

    @Override
    public TimeSeries getMetricData(final long metricId, final TimeGrouper grouper) throws MonitoringDBException {

        try (final DSLContext context = getContext()) {
            final Field<Long> bin = Tables.METRIC_VALUE.TIME.sub(grouper.getStartEpoch())
                .divide(grouper.getBinLength());
            final Result<Record3<BigDecimal, Integer, Long>> records = context
                .select(Tables.METRIC_VALUE.METRIC_VALUE_.avg(), Tables.METRIC_VALUE.METRIC_VALUE_.count(), bin)
                .from(Tables.METRIC_VALUE)
                .where(Tables.METRIC_VALUE.TIME.greaterThan(grouper.getStartEpoch()))
                .and(Tables.METRIC_VALUE.TIME.lessThan(grouper.getEndEpoch()))
                .groupBy(bin)
                .orderBy(bin)
                .fetch();
            final TimeSeries timeSeries = new TimeSeries();
            grouper.getPoints().forEach(value -> timeSeries.newPoint(value, 0).build()); // zero out series
            for (final Record3<BigDecimal, Integer, Long> record : records) {
                final long startEpoch = grouper.getBinStartTime(record.value3());
                final int count = record.value2();
                final float average = record.value1().floatValue();
                timeSeries.newPoint(startEpoch, count).average(average).build();
            }
            return timeSeries;
        } catch (MonitoringDBException e) {
            throw new MonitoringDBException(e);
        }
    }

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.valueOf(connectionProvider.getSqlVariant()),
            new Settings().withRenderNameStyle(RenderNameStyle.AS_IS));
    }
}
