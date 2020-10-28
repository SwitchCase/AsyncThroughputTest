package com.switchcase.asyncthroughput.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitBuilderModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    Retrofit.Builder retrofitBuilder(ObjectMapper objectMapper) {
        OkHttpClient httpClient = new Builder()
                .build();

        return new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper));
    }

    @Provides
    public ObjectMapper mapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}
