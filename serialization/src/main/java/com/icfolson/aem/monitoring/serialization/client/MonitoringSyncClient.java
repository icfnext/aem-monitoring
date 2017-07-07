package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.writer.CountersDatabase;
import com.icfolson.aem.monitoring.database.writer.EventsDatabase;
import com.icfolson.aem.monitoring.database.writer.MetricsDatabase;
import com.icfolson.aem.monitoring.database.writer.SystemDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Parameters;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.exception.MonitoringSyncException;
import com.icfolson.aem.monitoring.serialization.model.*;
import okhttp3.*;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MonitoringSyncClient {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringSyncClient.class);

    private static final String AUTH_HEADER = "Authorization";

    private static final long DEFAULT_SINCE = 0;
    private static final int DEFAULT_LIMIT = 1000;

    private final RemoteSystem system;
    private final ConnectionProvider connectionProvider;

    private final SystemDatabase systemDatabase;
    private final EventsDatabase eventsDatabase;
    private final MetricsDatabase metricsDatabase;
    private final CountersDatabase countersDatabase;

    private OkHttpClient client;

    private long eventsSince;
    private long metricsSince;
    private long countersSince;

    public MonitoringSyncClient(NamedRemoteSystem system, ConnectionProvider connectionProvider)
            throws MonitoringDBException {

        this.system = system;
        this.connectionProvider = connectionProvider;
        final String repositoryUuid = system.getUuid().toString();
        systemDatabase = new SystemDatabase(connectionProvider);
        final short systemId = systemDatabase.getId(repositoryUuid);
        eventsDatabase = new EventsDatabase(systemId, connectionProvider);
        metricsDatabase = new MetricsDatabase(systemId, connectionProvider);
        countersDatabase = new CountersDatabase(systemId, connectionProvider);
        eventsSince = eventsDatabase.getLatestEventTimestamp();
        metricsSince = metricsDatabase.getLatestMetricTimestamp();
        countersSince = countersDatabase.getLatestCounterTimestamp();
        updateClient();
    }

    public MonitoringSyncResult execute() throws MonitoringSyncException {
        MonitoringSyncResult result = new MonitoringSyncResult();
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(system.getHost())
                .port(system.getPort())
                .encodedPath(Paths.ALL_SERVLET_PATH)
                .addQueryParameter(Parameters.EVENTS_SINCE, Long.toString(eventsSince))
                .addQueryParameter(Parameters.METRICS_SINCE, Long.toString(metricsSince))
                .addQueryParameter(Parameters.COUNTERS_SINCE, Long.toString(countersSince))
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            final Response response = client.newCall(request).execute();
            final int code = response.code();
            if (code == SlingHttpServletResponse.SC_OK) {
                final InputStream inputStream = response.body().byteStream();
                try (DataInputStream stream = new DataInputStream(inputStream)) {
                    // TODO Do each entity type in parallel
                    final EventsTable eventsTable = EventsTable.readEvents(stream);
                    final List<MonitoringEvent> events = eventsTable.getEvents();
                    for (MonitoringEvent event : events) {
                        eventsDatabase.writeEvent(event);
                    }
                    result.setEventCount(events.size());
                    eventsSince = events.isEmpty() ? eventsSince : events.get(events.size() - 1).getTimestamp() + 1;
                    final MetricsTable metricsTable = MetricsTable.readMetrics(stream);
                    final List<MonitoringMetric> metrics = metricsTable.getMetrics();
                    for (MonitoringMetric metric : metrics) {
                        metricsDatabase.writeMetric(metric);
                    }
                    result.setMetricCount(metrics.size());
                    metricsSince = metrics.isEmpty() ? metricsSince :
                            metrics.get(metrics.size() - 1).getTimestamp() + 1;
                    final CountersTable countersTable = CountersTable.readCounters(stream);
                    final List<MonitoringCounter> counters = countersTable.getCounters();
                    for (MonitoringCounter counter : counters) {
                        countersDatabase.writeCounter(counter);
                    }
                    result.setCounterCount(counters.size());
                    countersSince = counters.isEmpty() ? countersSince :
                            counters.get(counters.size() - 1).getTimestamp() + 1;
                } catch (IOException e) {
                    throw new MonitoringSyncException(e);
                }
            } else {
                throw new MonitoringSyncException("Received error response code from remote system: " + code);
            }
        } catch (IOException e) {
            throw new MonitoringSyncException(e);
        }
        return result;
    }

    private void updateClient() {
        this.client = new OkHttpClient.Builder()
                .authenticator((route, response) -> response.request().newBuilder()
                        .addHeader(AUTH_HEADER, Credentials.basic(system.getUser(), system.getPassword()))
                        .build())
                .connectTimeout(1L, TimeUnit.MINUTES)
                .readTimeout(1L, TimeUnit.MINUTES)
                .writeTimeout(1L, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();
    }
}
