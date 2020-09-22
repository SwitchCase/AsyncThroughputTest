package com.switchcase.asyncthroughput.controller;

import com.switchcase.asyncthroughput.types.MilkSpec;
import com.switchcase.asyncthroughput.types.TeaSpec;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;


@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MilkTeaSpecRequest {
    MilkSpec milk;
    TeaSpec tea;
}
