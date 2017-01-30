package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.writer.EventsDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.EventsTable;
import com.icfolson.aem.monitoring.serialization.model.RemoteSystem;
import okhttp3.Request;

import java.io.IOException;
import java.io.InputStream;

public class EventsSyncClient extends AbstractSyncClient {

    private final RemoteSystem remoteSystem;
    private final ConnectionProvider connectionProvider;
    private final EventsDatabase database;

    public EventsSyncClient(final RemoteSystem remoteSystem, final ConnectionProvider connectionProvider) {
        this.remoteSystem = remoteSystem;
        this.connectionProvider = connectionProvider;
        this.database = new EventsDatabase(remoteSystem.getUuid(), connectionProvider);
    }

    @Override
    public void sync() {
        final Request request = new Request.Builder()
            .url(remoteSystem.getUrl() + Paths.EVENTS_SERVLET_PATH)
            .get()
            .build();
        try (final InputStream stream = executeRequest(request).body().byteStream()) {
            final EventsTable table = EventsTable.readEvents(stream);
            for (final MonitoringEvent monitoringEvent : table.getEvents()) {
                database.writeEvent(monitoringEvent); // TODO batch
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
