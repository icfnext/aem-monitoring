package com.icfolson.aem.monitoring.serialization.model;

import java.util.UUID;

public class RemoteSystem {

    private final UUID uuid;
    private final String url;
    private final String user;
    private final String password;

    public RemoteSystem(final UUID uuid, final String url, final String user, final String password) {
        this.uuid = uuid;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
