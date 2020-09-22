package com.switchcase.asyncthroughput.types;

import com.switchcase.asyncthroughput.client.response.MilkTeaResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ResponseD {
    Wrapper<MilkTeaResponse> d;
    Double total;
}
