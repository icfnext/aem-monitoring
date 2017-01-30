package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.writer.CountersDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.CountersTable;
import com.icfolson.aem.monitoring.serialization.model.RemoteSystem;
import okhttp3.Request;

import java.io.IOException;
import java.io.InputStream;

public class CountersSyncClient extends AbstractSyncClient {

    private final RemoteSystem remoteSystem;
    private final ConnectionProvider connectionProvider;
    private final CountersDatabase database;

    public CountersSyncClient(final RemoteSystem remoteSystem, final ConnectionProvider connectionProvider) {
        this.remoteSystem = remoteSystem;
        this.connectionProvider = connectionProvider;
        this.database = new CountersDatabase(remoteSystem.getUuid(), connectionProvider);
    }

    @Override
    public void sync() {
        final Request request = new Request.Builder()
            .url(remoteSystem.getUrl() + Paths.COUNTERS_SERVLET_PATH)
            .get()
            .build();
        try (final InputStream stream = executeRequest(request).body().byteStream()) {
            final CountersTable table = CountersTable.readCounters(stream);
            for (final MonitoringCounter monitoringCounter : table.getCounters()) {
                database.writeCounter(monitoringCounter); // TODO batch
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
