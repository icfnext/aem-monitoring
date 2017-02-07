package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.writer.CountersDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.CountersTable;
import com.icfolson.aem.monitoring.serialization.model.NamedRemoteSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class CountersSyncClient extends AbstractSyncClient {

    private static final Logger LOG = LoggerFactory.getLogger(CountersSyncClient.class);

    private static final long LIMIT = 1000;

    private final CountersDatabase database;
    private long since;

    public CountersSyncClient(final NamedRemoteSystem system, final ConnectionProvider connectionProvider) {
        super(system, connectionProvider);
        this.database = new CountersDatabase(system.getUuid(), connectionProvider);
        since = database.getLatestCounterTimestamp() + 1;
    }

    @Override
    public synchronized void sync() {
        try (final InputStream stream = executeRequest()) {
            final CountersTable table = CountersTable.readCounters(stream);
            for (final MonitoringCounter monitoringCounter : table.getCounters()) {
                if (since < monitoringCounter.getTimestamp() + 1) {
                    since = monitoringCounter.getTimestamp();
                }
                database.writeCounter(monitoringCounter); // TODO batch
            }
        } catch (IOException e) {
            LOG.error("Error writing counters", e);
        }
    }

    @Override
    protected String getRelativePath() {
        return Paths.COUNTERS_SERVLET_PATH;
    }

    @Override
    protected long getStartTimestamp() {
        return since;
    }

    @Override
    protected long getLimit() {
        return LIMIT;
    }
}
