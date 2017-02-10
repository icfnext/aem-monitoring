package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.writer.SystemDatabase;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import com.icfolson.aem.monitoring.serialization.model.SystemTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class SystemSyncClient extends AbstractSyncClient {

    private static final Logger LOG = LoggerFactory.getLogger(SystemSyncClient.class);

    private final SystemDatabase systemDatabase;
    private UUID uuid;

    public SystemSyncClient(final RemoteSystem system, final ConnectionProvider connectionProvider) {
        super(system, connectionProvider);
        this.systemDatabase = new SystemDatabase(connectionProvider);
    }

    /**
     * Returns the UUID of the remote system.  Only available after {@link #sync()} has been called.
     * @return
     */
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public synchronized void sync() {
        try ( final InputStream stream = executeRequest()) {
            final SystemTable table = SystemTable.readTable(stream);
            uuid = table.getUuid();
            systemDatabase.writeSystem(table);
        } catch (IOException e) {
            LOG.error("Error reading data from remote system", e);
        }
    }

    @Override
    protected String getRelativePath() {
        return Paths.SYSTEM_SERVLET_PATH;
    }

    @Override
    protected long getStartTimestamp() {
        return 0;
    }

    @Override
    protected long getLimit() {
        return 0;
    }
}
