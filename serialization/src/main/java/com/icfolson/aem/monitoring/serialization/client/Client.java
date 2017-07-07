package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.serialization.exception.MonitoringSyncException;
import com.icfolson.aem.monitoring.serialization.model.MonitoringSyncResult;
import com.icfolson.aem.monitoring.serialization.model.NamedRemoteSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private static final QualifiedName EVENT_NAME = new QualifiedName("monitoring", "client", "sync");

    private final RemoteSystem system;
    private final ConnectionProvider connectionProvider;
    private final MonitoringService monitoringService;
    private final Timer timer = new Timer("monitoringSyncClient");
    private MonitoringSyncClient client;

    public Client(final RemoteSystem system, final ConnectionProvider connectionProvider, MonitoringService monitoringService) {
        this.system = system;
        this.connectionProvider = connectionProvider;
        this.monitoringService = monitoringService;
        start();
    }

    public void execute() throws MonitoringSyncException {
        try {
            if (client != null) {
                monitoringService.initializeTransaction(EVENT_NAME);
                monitoringService.setTransactionProperty("client.host", system.getHost());
                monitoringService.setTransactionProperty("client.port", Integer.toString(system.getPort()));

                final MonitoringSyncResult syncResult = client.execute();

                monitoringService.setTransactionProperty("event.total", syncResult.getEventCount());
                monitoringService.setTransactionProperty("metric.total", syncResult.getMetricCount());
                monitoringService.setTransactionProperty("counter.total", syncResult.getCounterCount());

                monitoringService.recordTransaction();
            }
        } catch (Throwable e) {
            LOG.error("Error syncing with remote client: " + system.getHost(), e);
            throw new MonitoringSyncException(e);
        }
    }

    private void start() {
        // Initialize client (async)
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final SystemSyncClient systemSyncClient = new SystemSyncClient(system, connectionProvider);
                systemSyncClient.sync();
                final NamedRemoteSystem namedRemoteSystem = new NamedRemoteSystem(systemSyncClient.getUuid(), system);
                try {
                    client = new MonitoringSyncClient(namedRemoteSystem, connectionProvider);
                } catch (MonitoringDBException e) {
                    LOG.error("Error initializing client", e);
                }
            }
        }, 0L);
    }

}
