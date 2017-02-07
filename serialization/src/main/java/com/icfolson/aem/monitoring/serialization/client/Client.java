package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.serialization.model.NamedRemoteSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Client {

    private static final QualifiedName COUNTER_NAME = new QualifiedName("Meta", "Sync", "Client");

    private final List<SyncClient> clients = new ArrayList<>();
    private final RemoteSystem system;
    private final ConnectionProvider connectionProvider;
    private final MonitoringService monitoringService;
    private final Timer timer = new Timer("monitoringSyncClient");

    public Client(final RemoteSystem system, final ConnectionProvider connectionProvider, MonitoringService monitoringService) {
        this.system = system;
        this.connectionProvider = connectionProvider;
        this.monitoringService = monitoringService;
        start();
    }

    public void execute() {
        for (final SyncClient client : clients) {
            client.sync();
        }
        monitoringService.incrementCounter(COUNTER_NAME, 1);
    }

    private void start() {
        // Initialize sub-clients (async)
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final SystemSyncClient systemSyncClient = new SystemSyncClient(system, connectionProvider);
                systemSyncClient.sync();
                final NamedRemoteSystem namedRemoteSystem = new NamedRemoteSystem(systemSyncClient.getUuid(), system);
                clients.add(new EventsSyncClient(namedRemoteSystem, connectionProvider));
                clients.add(new MetricsSyncClient(namedRemoteSystem, connectionProvider));
                clients.add(new CountersSyncClient(namedRemoteSystem, connectionProvider));
            }
        }, 0L);
    }

}
