package com.switchcase.asyncthroughput;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchcase.asyncthroughput.client.TeaServiceAsyncClient;
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
import java.util.concurrent.ExecutorService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsynchronousService {
    private static final Logger logger = LoggerFactory.getLogger(AsynchronousService.class);

    private final TeaServiceAsyncClient teaServiceClient;
    private final ObjectMapper mapper;
    private final ExecutorService executorService;

    public AsynchronousService(@Autowired TeaServiceAsyncClient teaServiceClient, @Autowired ObjectMapper mapper, @Autowired ExecutorService executorService) {
        this.teaServiceClient = teaServiceClient;
        this.mapper = mapper;
        this.executorService = executorService;
    }

    @Async("asyncExec")
    public CompletableFuture<MilkTeaResponse> executeAsync(String id, MilkTeaSpecRequest request) {
        logger.info("Running id = {}", id);
        return invoke(request);
    }

    private CompletableFuture<MilkTeaResponse> invoke(MilkTeaSpecRequest request) {
        logger.info("Invoked request: {}", request);
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "start", executorService);
        CompletableFuture<BrewTeaResponse> chain1 = cf.thenComposeAsync(r -> boilWater(request.getTea().getQuantity()), executorService)
                .thenComposeAsync(w -> brewTea(w.getWater(), request.getTea()), executorService);
        CompletableFuture<BoilMilkResponse> chain2 = cf.thenComposeAsync(r -> boilMilk(request.getMilk()), executorService);

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
}
