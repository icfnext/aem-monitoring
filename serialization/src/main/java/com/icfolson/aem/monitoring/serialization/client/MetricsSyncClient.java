package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.writer.MetricsDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.MetricsTable;
import com.icfolson.aem.monitoring.serialization.model.NamedRemoteSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class MetricsSyncClient extends AbstractSyncClient {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsSyncClient.class);

    private static final long LIMIT = 1000;

    private final MetricsDatabase metricsDatabase;
    private long since;

    public MetricsSyncClient(final NamedRemoteSystem system, final ConnectionProvider connectionProvider) {
        super(system, connectionProvider);
        this.metricsDatabase = new MetricsDatabase(system.getUuid(), connectionProvider);
        since = this.metricsDatabase.getLatestMetricTimestamp() + 1;
    }

    @Override
    public synchronized void sync() {
        try ( final InputStream stream = executeRequest()) {
            final MetricsTable table = MetricsTable.readMetrics(stream);
            for (final MonitoringMetric monitoringMetric : table.getMetrics()) {
                if (since < monitoringMetric.getTimestamp() + 1) {
                    since = monitoringMetric.getTimestamp();
                }
                metricsDatabase.writeMetric(monitoringMetric); // TODO batch
            }
        } catch (IOException e) {
            LOG.error("Error writing metrics", e);
        }
    }

    @Override
    protected String getRelativePath() {
        return Paths.METRICS_SERVLET_PATH;
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
