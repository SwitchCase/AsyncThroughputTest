package com.switchcase.asyncthroughput.client;

import com.switchcase.asyncthroughput.client.request.BoilMilkRequest;
import com.switchcase.asyncthroughput.client.request.BoilWaterRequest;
import com.switchcase.asyncthroughput.client.request.BrewTeaRequest;
import com.switchcase.asyncthroughput.client.request.MilkTeaRequest;
import com.switchcase.asyncthroughput.client.response.BoilMilkResponse;
import com.switchcase.asyncthroughput.client.response.BoilWaterResponse;
import com.switchcase.asyncthroughput.client.response.BrewTeaResponse;
import com.switchcase.asyncthroughput.client.response.MilkTeaResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TeaServiceClient {

    @POST("boil-water")
    Call<BoilWaterResponse> boilWater(@Body BoilWaterRequest request);

    @POST("boil-milk")
    Call<BoilMilkResponse> boilMilk(@Body BoilMilkRequest request);

    @POST("brew-tea")
    Call<BrewTeaResponse> brewTea(@Body BrewTeaRequest request);

    @POST("combine-milk-tea")
    Call<MilkTeaResponse> combineMilkTea(@Body MilkTeaRequest request);
}
