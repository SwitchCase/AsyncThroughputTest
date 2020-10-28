package com.switchcase.asyncthroughput;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchcase.asyncthroughput.client.request.BoilMilkRequest;
import com.switchcase.asyncthroughput.client.request.BoilWaterRequest;
import com.switchcase.asyncthroughput.client.request.BrewTeaRequest;
import com.switchcase.asyncthroughput.client.request.MilkTeaRequest;
import com.switchcase.asyncthroughput.client.response.BoilMilkResponse;
import com.switchcase.asyncthroughput.client.response.BoilWaterResponse;
import com.switchcase.asyncthroughput.client.response.BrewTeaResponse;
import com.switchcase.asyncthroughput.client.response.MilkTeaResponse;
import com.switchcase.asyncthroughput.controller.MilkTeaSpecRequest;
import com.switchcase.asyncthroughput.types.BoiledMilk;
import com.switchcase.asyncthroughput.types.BoiledWater;
import com.switchcase.asyncthroughput.types.BrewedTea;
import com.switchcase.asyncthroughput.types.MilkSpec;
import com.switchcase.asyncthroughput.types.TeaSpec;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.SneakyThrows;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsynchronousServicev2 {
    private static final Logger logger = LoggerFactory.getLogger(AsynchronousServicev2.class);

    private final ObjectMapper mapper;
    private final AsyncHttpClient httpClient;
    private final Executor executorService;

    @Inject
    public AsynchronousServicev2(ObjectMapper mapper, AsyncHttpClient httpClient, @Named("asyncExec") Executor executorService) {
        this.mapper = mapper;
        this.httpClient = httpClient;
        this.executorService = executorService;
    }

    public CompletableFuture<MilkTeaResponse> executeAsync(String id, MilkTeaSpecRequest request) {
        logger.info("Running id = {}", id);
        //ideally - i want to run invoke() here which is a true async http client. But doing that seems to be worse than
        // the invokeSync with blocking. For some reason retrofit does not like working with CFs.
        return invoke(request);
    }

    private CompletableFuture<MilkTeaResponse> invoke(MilkTeaSpecRequest request) {
        //logger.info("Invoked request: {}", request);
        CompletableFuture<BrewTeaResponse> chain1 = boilWater(request.getTea().getQuantity())
                                                        .thenComposeAsync(w -> brewTea(w.getWater(), request.getTea()), executorService);
        CompletableFuture<BoilMilkResponse> chain2 = boilMilk(request.getMilk());

        return CompletableFuture.allOf(chain1, chain2).thenComposeAsync(r -> {
            try {
                return combineTeaAndMilk(chain1.get().getTea(), chain2.get().getMilk());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error executing invokeD. ", e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @SneakyThrows
    private CompletableFuture<BoilWaterResponse> boilWater(int quantity) {
        return CompletableFuture.supplyAsync(() -> {
            RequestBuilder request = new RequestBuilder();
            request.setBody(writeSafely(new BoilWaterRequest(quantity)));
            request.setMethod("POST");
            request.setUrl("http://localhost:9900/boil-water");
            return request;
        }, executorService).thenComposeAsync(request -> executeHttpCall(request.build(), BoilWaterResponse.class), executorService);
    }

    @SneakyThrows
    private CompletableFuture<BrewTeaResponse> brewTea(BoiledWater water, TeaSpec teaSpec) {
        return CompletableFuture.supplyAsync(() -> {
            RequestBuilder request = new RequestBuilder();
            request.setBody(writeSafely(new BrewTeaRequest(water, teaSpec.getQuantity(), teaSpec.getType(), teaSpec.getName())));
            request.setMethod("POST");
            request.setUrl("http://localhost:9900/brew-tea");
            return request;
        }, executorService).thenComposeAsync(request -> executeHttpCall(request.build(), BrewTeaResponse.class), executorService);
    }

    @SneakyThrows
    private CompletableFuture<BoilMilkResponse> boilMilk(MilkSpec spec) {
        return CompletableFuture.supplyAsync(() -> {
            RequestBuilder request = new RequestBuilder();
            request.setBody(writeSafely(new BoilMilkRequest(spec.getQuantity(), spec.getType())));
            request.setUrl("http://localhost:9900/boil-milk");
            request.setMethod("POST");
            return request;
        }, executorService).thenComposeAsync(request -> executeHttpCall(request.build(), BoilMilkResponse.class), executorService);
    }

    @SneakyThrows
    private CompletableFuture<MilkTeaResponse> combineTeaAndMilk(BrewedTea tea, BoiledMilk milk) {
        return CompletableFuture.supplyAsync(() -> {
            //logger.info("Combining milk and tea: {}, {}", tea, milk);
            RequestBuilder request = new RequestBuilder();
            request.setUrl("http://localhost:9900/combine-milk-tea");
            request.setBody(writeSafely(new MilkTeaRequest(tea, milk)));
            request.setMethod("POST");
            return request;
        }, executorService).thenComposeAsync(request -> executeHttpCall(request.build(), MilkTeaResponse.class), executorService);
    }

    private <T> CompletableFuture<T> executeHttpCall(Request request, Class<T> klass) {
        return httpClient.prepareRequest(request).execute(new AsyncCompletionHandler<T>() {
            @Override
            public T onCompleted(org.asynchttpclient.Response response) throws Exception {
                if (response.getStatusCode() == 200) {
                    return readSafely(response.getResponseBody(), klass);
                } else {
                    throw new RuntimeException("Non 200 response received. Received = " + response.getStatusCode());
                }
            }
        }).toCompletableFuture();
    }

    @SneakyThrows
    private <T> T readSafely(String val, Class<T> klass) {
        return mapper.readValue(val, klass);
    }

    @SneakyThrows
    private<T> String writeSafely(T obj) {
        return mapper.writeValueAsString(obj);
    }


}
