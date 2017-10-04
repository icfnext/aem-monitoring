package com.icfolson.aem.monitoring.serialization.client;

import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSyncClient implements SyncClient {

    private static final String AUTH_HEADER = "Authorization";

    private OkHttpClient client;

    private final RemoteSystem system;
    private final ConnectionProvider connectionProvider;

    public AbstractSyncClient(final RemoteSystem system, final ConnectionProvider connectionProvider) {
        this.system = system;
        this.connectionProvider = connectionProvider;
        this.updateClient();
    }

    protected final void updateClient() {

        this.client = new OkHttpClient.Builder()
            .authenticator(new Authenticator() {
                @Override
                public Request authenticate(final Route route, final Response response) throws IOException {
                    return response.request().newBuilder()
                        .addHeader(AUTH_HEADER, Credentials.basic(system.getUser(), system.getPassword()))
                        .build();
                }
            })
            .connectTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build();
    }

    protected final InputStream executeRequest() throws IOException {
        final HttpUrl url = new HttpUrl.Builder()
            .scheme("http")
            .host(system.getHost())
            .port(system.getPort())
            .encodedPath(getRelativePath())
            .build();
        final Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        return client.newCall(request).execute().body().byteStream();
    }

    protected final ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    protected abstract String getRelativePath();

}
