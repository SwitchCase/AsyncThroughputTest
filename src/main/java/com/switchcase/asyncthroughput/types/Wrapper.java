package com.switchcase.asyncthroughput.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Wrapper<T> {
    @JsonProperty("slept_duration")
    private final double sleptDuration;

    @JsonProperty("input")
    private final T input;

    @JsonCreator
    public Wrapper(@JsonProperty("slept_duration") double sleptDuration,
                   @JsonProperty("input") T input) {
        this.sleptDuration = sleptDuration;
        this.input = input;
    }
}
