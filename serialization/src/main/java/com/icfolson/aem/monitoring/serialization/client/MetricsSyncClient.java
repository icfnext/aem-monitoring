package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.writer.MetricsDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.MetricsTable;
import com.icfolson.aem.monitoring.serialization.model.RemoteSystem;
import okhttp3.Request;

import java.io.IOException;
import java.io.InputStream;

public class MetricsSyncClient extends AbstractSyncClient {

    private final RemoteSystem remoteSystem;
    private final ConnectionProvider connectionProvider;
    private final MetricsDatabase metricsDatabase;

    public MetricsSyncClient(final RemoteSystem remoteSystem, final ConnectionProvider connectionProvider) {
        this.remoteSystem = remoteSystem;
        this.connectionProvider = connectionProvider;
        this.metricsDatabase = new MetricsDatabase(remoteSystem.getUuid(), connectionProvider);
    }

    @Override
    public void sync() {
        final Request request = new Request.Builder()
            .url(remoteSystem.getUrl() + Paths.METRICS_SERVLET_PATH)
            .get()
            .build();
        try ( final InputStream stream = executeRequest(request).body().byteStream()) {
            final MetricsTable table = MetricsTable.readMetrics(stream);
            for (final MonitoringMetric monitoringMetric : table.getMetrics()) {
                metricsDatabase.writeMetric(monitoringMetric); // TODO batch
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
