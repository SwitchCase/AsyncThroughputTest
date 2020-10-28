package com.switchcase.asyncthroughput;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.switchcase.asyncthroughput.client.RetrofitBuilderModule;
import com.switchcase.asyncthroughput.client.TeaServiceAsyncClient;
import com.switchcase.asyncthroughput.client.TeaServiceAsyncClientModule;
import com.switchcase.asyncthroughput.client.TeaServiceClient;
import com.switchcase.asyncthroughput.client.TeaServiceClientModule;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import org.asynchttpclient.AsyncHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AsyncThroughputTestApp {

    final Injector INJECTOR = Guice.createInjector(new TeaServiceAsyncClientModule(), new TeaServiceClientModule(), new RetrofitBuilderModule());

    public static void main(String[] args) {
        // close the application context to shut down the custom ExecutorService
        SpringApplication.run(AsyncThroughputTestApp.class, args);
    }

    @Bean("asyncExec")
    public Executor taskExecutor() {
        ForkJoinPool.getCommonPoolParallelism();
        return ForkJoinPool.commonPool();

        //ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        //return Executors.newFixedThreadPool(100, builder.setNameFormat("async-exec-%d").build());
    }

    @Bean("syncExec")
    public ExecutorService syncTaskExecutor() {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        //return ForkJoinPool.commonPool();
        return Executors.newFixedThreadPool(100, builder.setNameFormat("sync-exec-%d").build());
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                   .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public AsyncHttpClient asyncHttpClient() {
        return org.asynchttpclient.Dsl.asyncHttpClient();
    }

    @Bean
    public TeaServiceAsyncClient teaServiceAsyncClient() {
        return INJECTOR.getInstance(TeaServiceAsyncClient.class);
    }

    @Bean
    public TeaServiceClient teaServiceClient() {
        return INJECTOR.getInstance(TeaServiceClient.class);
    }

    @Bean
    public AsynchronousServicev2 asynchronousServicev2(ObjectMapper mapper, AsyncHttpClient httpClient) {
        return new AsynchronousServicev2(mapper, httpClient, taskExecutor());
    }

    @Bean
    public SynchronousService synchronousService(TeaServiceClient teaServiceClient) {
        return new SynchronousService(teaServiceClient, syncTaskExecutor());
    }
}
