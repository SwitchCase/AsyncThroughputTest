package com.switchcase.asyncthroughput.client;

import com.switchcase.asyncthroughput.client.request.BoilMilkRequest;
import com.switchcase.asyncthroughput.client.request.BoilWaterRequest;
import com.switchcase.asyncthroughput.client.request.BrewTeaRequest;
import com.switchcase.asyncthroughput.client.request.MilkTeaRequest;
import com.switchcase.asyncthroughput.client.response.BoilMilkResponse;
import com.switchcase.asyncthroughput.client.response.BoilWaterResponse;
import com.switchcase.asyncthroughput.client.response.BrewTeaResponse;
import com.switchcase.asyncthroughput.client.response.MilkTeaResponse;
import java.util.concurrent.CompletableFuture;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TeaServiceAsyncClient {

    @POST("boil-water")
    CompletableFuture<BoilWaterResponse> boilWater(@Body BoilWaterRequest request);

    @POST("boil-milk")
    CompletableFuture<BoilMilkResponse> boilMilk(@Body BoilMilkRequest request);

    @POST("brew-tea")
    CompletableFuture<BrewTeaResponse> brewTea(@Body BrewTeaRequest request);

    @POST("combine-milk-tea")
    CompletableFuture<MilkTeaResponse> combineMilkTea(@Body MilkTeaRequest request);
}
