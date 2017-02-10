package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.writer.EventsDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.EventsTable;
import com.icfolson.aem.monitoring.serialization.model.NamedRemoteSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class EventsSyncClient extends AbstractSyncClient {

    private static final Logger LOG = LoggerFactory.getLogger(EventsSyncClient.class);

    private static final long LIMIT = 1000;

    private final EventsDatabase database;
    private long since;

    public EventsSyncClient(final NamedRemoteSystem system, final ConnectionProvider connectionProvider) {
        super(system, connectionProvider);
        this.database = new EventsDatabase(system.getUuid(), connectionProvider);
        since = database.getLatestEventTimestamp() + 1;
    }

    @Override
    public synchronized void sync() {
        try (final InputStream stream = executeRequest()) {
            final EventsTable table = EventsTable.readEvents(stream);
            for (final MonitoringEvent monitoringEvent : table.getEvents()) {
                if (since < monitoringEvent.getTimestamp() + 1) {
                    since = monitoringEvent.getTimestamp();
                }
                database.writeEvent(monitoringEvent); // TODO batch
            }
        } catch (IOException e) {
            LOG.error("Error writing events", e);
        }
    }

    @Override
    protected String getRelativePath() {
        return Paths.EVENTS_SERVLET_PATH;
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
