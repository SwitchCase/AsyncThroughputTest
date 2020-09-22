package com.switchcase.asyncthroughput.types;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MilkTea {
    double timeTaken;
    String id;
    BoiledMilk milk;
    BrewedTea tea;
    int quantity;
}
