package com.icfolson.aem.monitoring.serialization.model;

import javax.inject.Inject;
import java.util.UUID;

public class NamedRemoteSystem extends RemoteSystem {

    private final UUID uuid;
    private final RemoteSystem system;

    public NamedRemoteSystem(final UUID uuid, final RemoteSystem system) {
        this.uuid = uuid;
        this.system = system;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    @Inject
    public String getHost() {
        return system.getHost();
    }

    @Override
    @Inject
    public int getPort() {
        return system.getPort();
    }

    @Override
    @Inject
    public String getUser() {
        return system.getUser();
    }

    @Override
    @Inject
    public String getPassword() {
        return system.getPassword();
    }
}
