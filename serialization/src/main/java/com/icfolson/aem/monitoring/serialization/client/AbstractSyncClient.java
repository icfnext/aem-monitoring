package com.icfolson.aem.monitoring.serialization.client;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSyncClient {

    private static final String AUTH_HEADER = "Authorization";

    private OkHttpClient client;

    protected abstract void sync();

    protected final void updateClient(final String url, final String user, final String password) {

        this.client = new OkHttpClient.Builder()
            .authenticator(new Authenticator() {
                @Override
                public Request authenticate(final Route route, final Response response) throws IOException {
                    return response.request().newBuilder()
                        .addHeader(AUTH_HEADER, Credentials.basic(user, password))
                        .build();
                }
            })
            .connectTimeout(1L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .writeTimeout(1L, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build();
    }

    protected final Response executeRequest(final Request request) throws IOException {
        return client.newCall(request).execute();
    }


}
