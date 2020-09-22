package com.switchcase.asyncthroughput.types;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BrewedTea {
    double timeTaken;
    String id;
    int quantity;
    String type;
    String name;
    BoiledWater water;
}
