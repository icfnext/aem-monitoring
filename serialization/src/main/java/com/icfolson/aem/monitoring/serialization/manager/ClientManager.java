package com.icfolson.aem.monitoring.serialization.manager;

import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.serialization.client.Client;
import com.icfolson.aem.monitoring.serialization.exception.MonitoringSyncException;
import org.apache.felix.scr.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service(ClientManager.class)
@Component(immediate = true)
@Property(name = "scheduler.period", longValue = 20)
public class ClientManager implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ClientManager.class);

    @Reference
    private ClientDataRepository repository;

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private MonitoringService monitoringService;

    private final Map<RemoteSystem, Client> clients = new HashMap<>();

    public void stopClients() {
        clients.clear();
    }

    public void restartClients() {
        stopClients();
        startClients();
    }

    public void startClients() {
        final Map<String, RemoteSystem> configuredSystems = repository.getConfiguredSystems();
        configuredSystems.values().forEach(system -> clients.put(system,
                new Client(system, connectionProvider, monitoringService)));
    }

    @Activate
    protected final void activate() {
        startClients();
    }

    @Modified
    protected final void modified() {
        restartClients();
    }

    @Deactivate
    protected final void deactivate() {
        stopClients();
    }


    @Override
    public void run() {
        for (Client client : clients.values()) {
            try {
                client.execute();
            } catch (MonitoringSyncException e) {
                LOG.error("Error syncing client", e);
            }
        }
    }
}
