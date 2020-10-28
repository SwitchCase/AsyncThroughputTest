package com.switchcase.asyncthroughput;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchcase.asyncthroughput.client.TeaServiceAsyncClient;
import com.switchcase.asyncthroughput.client.TeaServiceClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import retrofit2.Response;

public class AsynchronousService {
    private static final Logger logger = LoggerFactory.getLogger(AsynchronousService.class);

    private final TeaServiceAsyncClient teaServiceClient;
    private final TeaServiceClient syncClient;
    private final ObjectMapper mapper;
    private final Executor executorService;

    @Inject
    public AsynchronousService(TeaServiceAsyncClient teaServiceClient,
                               TeaServiceClient syncClient,
                               ObjectMapper mapper,
                               @Named("asyncExec") Executor executorService) {
        this.teaServiceClient = teaServiceClient;
        this.syncClient = syncClient;
        this.mapper = mapper;
        this.executorService = executorService;
    }

    @Async("asyncExec")
    public CompletableFuture<MilkTeaResponse> executeAsync(String id, MilkTeaSpecRequest request) {
        logger.info("Running id = {}", id);
        //ideally - i want to run invoke() here which is a true async http client. But doing that seems to be worse than
        // the invokeSync with blocking. For some reason retrofit does not like working with CFs.
        return invoke(request);
    }

    private CompletableFuture<MilkTeaResponse> invoke(MilkTeaSpecRequest request) {
        logger.info("Invoked request: {}", request);
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

    private CompletableFuture<MilkTeaResponse> invokeSync(MilkTeaSpecRequest request) {
        logger.info("Invoked request: {}", request);
        CompletableFuture<BrewTeaResponse> chain1 = boilWaterSync(request.getTea().getQuantity())
                                                        .thenComposeAsync(w -> brewTeaSync(w.getWater(), request.getTea()));
        CompletableFuture<BoilMilkResponse> chain2 = boilMilkSync(request.getMilk());

        return CompletableFuture.allOf(chain1, chain2).thenComposeAsync(r -> {
            try {
                return combineTeaAndMilkSync(chain1.get().getTea(), chain2.get().getMilk());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error executing invokeD. ", e);
                throw new RuntimeException(e);
            }
        });
    }

    @SneakyThrows
    private CompletableFuture<BoilWaterResponse> boilWater(int quantity) {
        return teaServiceClient.boilWater(new BoilWaterRequest(quantity));
    }

    @SneakyThrows
    private CompletableFuture<BrewTeaResponse> brewTea(BoiledWater water, TeaSpec teaSpec) {
        BrewTeaRequest request = new BrewTeaRequest(water, teaSpec.getQuantity(), teaSpec.getType(), teaSpec.getName());
        return teaServiceClient.brewTea(request);
    }

    @SneakyThrows
    private CompletableFuture<BoilMilkResponse> boilMilk(MilkSpec spec) {
        BoilMilkRequest request = new BoilMilkRequest(spec.getQuantity(), spec.getType());
        return teaServiceClient.boilMilk(request);
    }

    @SneakyThrows
    private CompletableFuture<MilkTeaResponse> combineTeaAndMilk(BrewedTea tea, BoiledMilk milk) {
        logger.info("Combining milk and tea: {}, {}", tea, milk);
        MilkTeaRequest request = new MilkTeaRequest(tea, milk);
        return teaServiceClient.combineMilkTea(request);
    }

    @SneakyThrows
    private CompletableFuture<BoilWaterResponse> boilWaterSync(int quantity) {
        return CompletableFuture.supplyAsync(() -> requestHandler(() -> syncClient.boilWater(new BoilWaterRequest(quantity)).execute()));
    }

    @SneakyThrows
    private CompletableFuture<BrewTeaResponse> brewTeaSync(BoiledWater water, TeaSpec teaSpec) {
        return CompletableFuture.supplyAsync(() -> {
            BrewTeaRequest request = new BrewTeaRequest(water, teaSpec.getQuantity(), teaSpec.getType(), teaSpec.getName());
            return requestHandler(() -> syncClient.brewTea(request).execute());
        });
    }

    @SneakyThrows
    private CompletableFuture<BoilMilkResponse> boilMilkSync(MilkSpec spec) {
        return CompletableFuture.supplyAsync(() -> {
            BoilMilkRequest request = new BoilMilkRequest(spec.getQuantity(), spec.getType());
            return requestHandler(() -> syncClient.boilMilk(request).execute());
        });
    }

    @SneakyThrows
    private CompletableFuture<MilkTeaResponse> combineTeaAndMilkSync(BrewedTea tea, BoiledMilk milk) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Combining milk and tea: {}, {}", tea, milk);
            MilkTeaRequest request = new MilkTeaRequest(tea, milk);
            return requestHandler(() -> syncClient.combineMilkTea(request).execute());
        });
    }

    @SneakyThrows
    private<T> T requestHandler(SupplierWithException<Response<T>> supplier) {
        Response<T> response = supplier.get();
        if(response.isSuccessful()) {
            return response.body();
        } else {
            throw new RuntimeException(response.errorBody().string());
        }
    }

    @Async("asyncExec")
    public CompletableFuture<Boolean> veryLongMethod() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(true);
    }

    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
