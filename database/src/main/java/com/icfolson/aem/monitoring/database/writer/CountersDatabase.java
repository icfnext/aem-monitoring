package com.icfolson.aem.monitoring.database.writer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterValueRecord;
import com.icfolson.aem.monitoring.database.model.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.util.NameUtil;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class CountersDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(CountersDatabase.class);

    private final UUID systemId;
    private final ConnectionProvider connectionProvider;
    private final BiMap<String, Short> counterIds = HashBiMap.create();

    public CountersDatabase(final UUID systemId, final ConnectionProvider connectionProvider) {
        this.systemId = systemId;
        this.connectionProvider = connectionProvider;
    }

    public BiMap<String, Short> getCounterNameHierarchy() {
        return HashBiMap.create(counterIds);
    }

    public void writeCounter(final MonitoringCounter counter) {
        final String name = NameUtil.toStorageFormat(counter.getName());
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final CounterValueRecord record = context.newRecord(Tables.COUNTER_VALUE);
            record.setSystemId(systemId);
            record.setTime(counter.getTimestamp());
            final short id = initCounter(name);
            record.setCounterId(id);
            record.setIncrementValue(counter.getIncrement());
            record.insert();
        } catch (MonitoringDBException e) {
            LOG.error("Error writing counter data", e);
        }
    }

    public List<MonitoringCounter> getCounters(final Long since, final Integer limit) {
        List<MonitoringCounter> out = new ArrayList<>();
        final Map<Short, String> inverse = counterIds.inverse();
        Map<Short, QualifiedName> nameIndex = new HashMap<>();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Result<CounterValueRecord> records = context.selectFrom(Tables.COUNTER_VALUE)
                .where(Tables.COUNTER_VALUE.TIME.greaterOrEqual(since)
                    .and(Tables.COUNTER_VALUE.SYSTEM_ID.eq(systemId))).limit(limit).fetch();
            for (final CounterValueRecord record : records) {
                final short counterId = record.getCounterId();
                final Function<Short, QualifiedName> function = id -> NameUtil.toName(inverse.get(id));
                final QualifiedName name = nameIndex.computeIfAbsent(counterId, function);
                final MonitoringCounter counter = new CounterRecordDecorator(name, record);
                out.add(counter);
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error loading counter data", e);
        }
        return out;
    }

    public long getLatestCounterTimestamp() {
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Long time = context.select(Tables.COUNTER_VALUE.TIME.max()).from(Tables.COUNTER_VALUE)
                .where(Tables.COUNTER_VALUE.SYSTEM_ID.eq(systemId)).fetchOne(0, Long.class);
            if (time != null) {
                return time;
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error getting latest counter timestamp", e);
        }
        return -1;
    }

    private void initCounters() {
        counterIds.clear();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
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
            try (ConnectionWrapper wrapper = getConnection()) {
                final DSLContext context = wrapper.getContext();
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

    private ConnectionWrapper getConnection() throws MonitoringDBException {
        return connectionProvider.getConnection();
    }

    private class CounterRecordDecorator implements MonitoringCounter {

        private final QualifiedName name;
        private final CounterValueRecord record;

        private CounterRecordDecorator(final QualifiedName name, final CounterValueRecord record) {
            this.name = name;
            this.record = record;
        }

        @Override
        public QualifiedName getName() {
            return name;
        }

        @Override
        public long getTimestamp() {
            return record.getTime();
        }

        @Override
        public int getIncrement() {
            return record.getIncrementValue();
        }
    }
}
