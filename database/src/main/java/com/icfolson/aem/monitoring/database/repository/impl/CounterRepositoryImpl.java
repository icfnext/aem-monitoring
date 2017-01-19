package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.SystemInfo;
import com.icfolson.aem.monitoring.database.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterValueRecord;
import com.icfolson.aem.monitoring.database.repository.CounterRepository;
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
public class CounterRepositoryImpl implements CounterRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MetricRepositoryImpl.class);

    @Reference
    private SystemInfoProvider systemInfoProvider;

    @Reference
    private ConnectionProvider connectionProvider;
    private final Map<String, Short> counterIds = Collections.synchronizedMap(new HashMap<>());

    private SystemInfo systemInfo;
    private final ZoneId systemZone = ZoneId.systemDefault();  // TODO get from SIP

    @Override
    public Map<String, Short> getCounterNameHierarchy() {
        initCounters();
        return new HashMap<>(counterIds);
    }

    @Override
    public void writeCounter(final MonitoringCounter counter) {
        final String name = NameUtil.toStorageFormat(counter.getName());
        try (DSLContext context = getContext()) {
            final CounterValueRecord record = context.newRecord(Tables.COUNTER_VALUE);
            record.setSystemId(systemInfo.getSystemId());
            record.setTime(counter.getTimestamp().atZone(systemZone).toInstant().toEpochMilli());
            final short id = initCounter(name);
            record.setCounterId(id);
            record.setIncrementValue(counter.getIncrement());
            record.insert();
        } catch (MonitoringDBException e) {
            LOG.error("Error writing counter data", e);
        }
    }

    @Activate
    @Modified
    protected final void modified() {
        systemInfo = systemInfoProvider.getSystemInfo();
        initCounters();
    }

    private void initCounters() {
        counterIds.clear();
        try (DSLContext context = getContext()) {
            final Result<CounterRecord> counterRecords = context
                .selectFrom(Tables.COUNTER)
                .orderBy(Tables.COUNTER.COUNTER_ID)
                .fetch();
            for (final CounterRecord counterRecord : counterRecords) {
                final String name = counterRecord.getCounterName();
                final Short id = counterRecord.getCounterId();
                counterIds.put(name, id);
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error fetching counter data", e);
        }
    }

    private synchronized short initCounter(final String joinedName) throws MonitoringDBException {
        Short id = counterIds.get(joinedName);
        if (id == null) {
            // Before attempting to add a new counter, make sure another system hasn't already done so
            initCounters();
            id = counterIds.get(joinedName);
        }
        if (id != null) {
            return id;
        } else {
            try (DSLContext context = getContext()) {
                final CounterRecord counterRecord = context.newRecord(Tables.COUNTER);
                counterRecord.setCounterName(joinedName);
                counterRecord.insert();
                counterRecord.refresh();
                short out = counterRecord.getCounterId();
                counterIds.put(joinedName, out);
                return out;
            } catch (MonitoringDBException e) {
                throw new MonitoringDBException(e);
            }
        }
    }

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.H2);
    }
}
