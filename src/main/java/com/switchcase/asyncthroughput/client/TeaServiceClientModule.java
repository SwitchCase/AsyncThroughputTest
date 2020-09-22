package com.switchcase.asyncthroughput.client;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import retrofit2.Retrofit;

public class TeaServiceClientModule extends AbstractModule {

    @Singleton
    @Provides
    TeaServiceClient teaServiceClient(Retrofit.Builder builder) {
        return builder.baseUrl("http://localhost:9900").build().create(TeaServiceClient.class);
    }

    @Override
    protected void configure() {
    }
}
