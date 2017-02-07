package com.icfolson.aem.monitoring.serialization.manager;

import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.serialization.client.Client;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true)
public class ClientManager {

    @Reference
    private ClientDataRepository repository;

    @Reference
    private ConnectionProvider connectionProvider;

    private final Map<RemoteSystem, Client> clients = new HashMap<>();

    public void restartClients() {
        final List<RemoteSystem> configuredSystems = repository.getConfiguredSystems();
        clients.clear();
        configuredSystems.forEach(system -> clients.put(system, new Client(system, connectionProvider)));
    }

    @Activate
    @Modified
    protected final void modified(final Map<String, Object> props) {
        restartClients();
    }

}
