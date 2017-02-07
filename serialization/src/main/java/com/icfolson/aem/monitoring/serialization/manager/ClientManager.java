package com.icfolson.aem.monitoring.serialization.manager;

import com.icfolson.aem.monitoring.core.service.MonitoringService;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.serialization.client.Client;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import org.apache.felix.scr.annotations.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Component(immediate = true)
@Property(name = "scheduler.period", longValue = 10)
public class ClientManager implements Runnable {

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
        final List<RemoteSystem> configuredSystems = repository.getConfiguredSystems();
        configuredSystems.forEach(system -> clients.put(system,
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
        clients.values().forEach(Client::execute);
    }
}
