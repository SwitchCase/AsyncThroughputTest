package com.switchcase.asyncthroughput.client.response;

import com.switchcase.asyncthroughput.types.BoiledWater;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BoilWaterResponse extends BaseResponse {
    BoiledWater water;

}
