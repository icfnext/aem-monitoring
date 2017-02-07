package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.serialization.model.NamedRemoteSystem;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Client {

    private final List<SyncClient> clients = new ArrayList<>();
    private final RemoteSystem system;
    private final ConnectionProvider connectionProvider;
    private Timer timer;

    public Client(final RemoteSystem system, final ConnectionProvider connectionProvider) {
        this.system = system;
        this.connectionProvider = connectionProvider;
        start();
    }

    public void start() {
        if (timer == null) {
            timer = new Timer("monitoringSyncClient");
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
            // Schedule recurring sync operations
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    execute();
                }
            }, new Date(), TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS));
        }
    }

    public void stop() {
        timer.cancel();
        timer = null;
    }

    private void execute() {
        for (final SyncClient client : clients) {
            client.sync();
        }
    }

}
