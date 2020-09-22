package com.switchcase.asyncthroughput.client.request;

import com.switchcase.asyncthroughput.types.BoiledWater;
import lombok.Value;

@Value
public class BrewTeaRequest {
    BoiledWater water;
    int quantity;
    String type;
    String name;
}
