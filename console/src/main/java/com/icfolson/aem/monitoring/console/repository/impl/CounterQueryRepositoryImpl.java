package com.icfolson.aem.monitoring.console.repository.impl;

import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.console.repository.CounterQueryRepository;
import com.icfolson.aem.monitoring.console.result.MetricsTimeSeries;
import com.icfolson.aem.monitoring.console.result.TimeSeries;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.jooq.*;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Component(immediate = true)
public class CounterQueryRepositoryImpl implements CounterQueryRepository {

    @Reference
    private ConnectionProvider connectionProvider;

    @Override
    public MetricsTimeSeries getCounterData(final short counterId, final TimeGrouper grouper)
        throws MonitoringDBException {

        final MetricsTimeSeries out = new MetricsTimeSeries();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Field<Long> bin = Tables.COUNTER_VALUE.TIME.sub(grouper.getStartEpoch())
                .div(grouper.getBinLength()).floor().as("BIN");
            final Result<Record3<UUID, BigDecimal, Long>> records = context
                .select(Tables.COUNTER_VALUE.SYSTEM_ID.cast(UUID.class),
                    Tables.COUNTER_VALUE.INCREMENT_VALUE.sum(), bin)
                .from(Tables.COUNTER_VALUE)
                .where(Tables.COUNTER_VALUE.TIME.greaterThan(grouper.getStartEpoch()))
                .and(Tables.COUNTER_VALUE.TIME.lessThan(grouper.getEndEpoch()))
                .and(Tables.COUNTER_VALUE.COUNTER_ID.eq(counterId))
                .groupBy(Tables.COUNTER_VALUE.SYSTEM_ID, bin)
                .orderBy(bin)
                .fetch();
            for (final Record3<UUID, BigDecimal, Long> record : records) {
                final UUID systemId = record.value1();
                TimeSeries timeSeries = out.getTimeSeries(systemId);
                if (timeSeries == null) {
                    TimeSeries ts = out.addFacet(systemId); // Temporary final variable for lambda
                    grouper.getPoints().forEach(value -> ts.newPoint(value, 0).build()); // zero out points
                    timeSeries = ts;
                }
                final long binNumber = record.value3();
                final long epoch = grouper.getBinStartTime(binNumber);
                final TimeSeries.PointBuilder pointBuilder = timeSeries.newPoint(epoch, 1);
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
