package com.switchcase.asyncthroughput.types;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BoiledMilk {
    double timeTaken;
    String id;
    int quantity;
    String type;
}
