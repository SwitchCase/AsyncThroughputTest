package com.switchcase.asyncthroughput.client.request;

import com.switchcase.asyncthroughput.types.BoiledMilk;
import com.switchcase.asyncthroughput.types.BrewedTea;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MilkTeaRequest {
    BrewedTea tea;
    BoiledMilk milk;
}
