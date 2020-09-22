package com.switchcase.asyncthroughput.client;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;

public class TeaServiceAsyncClientModule extends AbstractModule {

    @Singleton
    @Provides
    TeaServiceAsyncClient teaServiceAsyncClient(Retrofit.Builder builder) {
        return builder.baseUrl("http://localhost:9900")
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .build()
                .create(TeaServiceAsyncClient.class);
    }

    @Override
    protected void configure() {
    }
}
