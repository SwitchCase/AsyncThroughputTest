package com.switchcase.asyncthroughput.client.request;

import lombok.Value;

@Value
public class BoilMilkRequest {
    int quantity;
    String type;
}
