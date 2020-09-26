package com.switchcase.asyncthroughput;

import com.switchcase.asyncthroughput.client.TeaServiceClient;
import com.switchcase.asyncthroughput.client.request.BoilMilkRequest;
import com.switchcase.asyncthroughput.client.request.BoilWaterRequest;
import com.switchcase.asyncthroughput.client.request.BrewTeaRequest;
import com.switchcase.asyncthroughput.client.request.MilkTeaRequest;
import com.switchcase.asyncthroughput.controller.MilkTeaSpecRequest;
import com.switchcase.asyncthroughput.types.BoiledMilk;
import com.switchcase.asyncthroughput.types.BoiledWater;
import com.switchcase.asyncthroughput.types.BrewedTea;
import com.switchcase.asyncthroughput.types.MilkSpec;
import com.switchcase.asyncthroughput.types.MilkTea;
import com.switchcase.asyncthroughput.types.TeaSpec;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

@org.springframework.stereotype.Service
public class SynchronousService {
    private static final Logger logger = LoggerFactory.getLogger(SynchronousService.class);

    private final TeaServiceClient teaServiceClient;
    private final ExecutorService executorService;

    public SynchronousService(TeaServiceClient teaServiceClient,
                              ExecutorService executorService) {
        this.teaServiceClient = teaServiceClient;
        this.executorService = executorService;
    }

    public MilkTea execute(String id, MilkTeaSpecRequest request) {
        logger.info("Running id = {}", id);
        return invoke(request);
    }

    private MilkTea invoke(MilkTeaSpecRequest request) {
        Future<BoiledMilk> boiledMilkFuture = executorService.submit(() -> boilMilk(request.getMilk()));
        Future<BrewedTea> brewedTeaFuture = executorService.submit(() -> brewTea(boilWater(request.getTea().getQuantity()), request.getTea()));
        Future<MilkTea> milkTeaFuture = executorService.submit(() -> combineTeaAndMilk(brewedTeaFuture.get(), boiledMilkFuture.get()));
        try {
            return milkTeaFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Exception in execution. ", e);
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private BoiledWater boilWater(int quantity) {
        return requestHandler(teaServiceClient.boilWater(new BoilWaterRequest(quantity)).execute()).getWater();
    }

    @SneakyThrows
    private BrewedTea brewTea(BoiledWater water, TeaSpec teaSpec) {
        BrewTeaRequest request = new BrewTeaRequest(water, teaSpec.getQuantity(), teaSpec.getType(), teaSpec.getName());
        return requestHandler(teaServiceClient.brewTea(request).execute()).getTea();
    }

    @SneakyThrows
    private BoiledMilk boilMilk(MilkSpec spec) {
        BoilMilkRequest request = new BoilMilkRequest(spec.getQuantity(), spec.getType());
        return requestHandler(teaServiceClient.boilMilk(request).execute()).getMilk();
    }

    @SneakyThrows
    private MilkTea combineTeaAndMilk(BrewedTea tea, BoiledMilk milk) {
        MilkTeaRequest request = new MilkTeaRequest(tea, milk);
        return requestHandler(teaServiceClient.combineMilkTea(request).execute()).getMilkTea();
    }

    @SneakyThrows
    private<T> T requestHandler(Response<T> response) {
        if(response.isSuccessful()) {
            return response.body();
        } else {
            throw new RuntimeException(response.errorBody().string());
        }
    }
}
