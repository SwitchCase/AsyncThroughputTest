package com.switchcase.asyncthroughput.controller;

import com.switchcase.asyncthroughput.AsynchronousService;
import com.switchcase.asyncthroughput.SynchronousService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

    @Autowired
    private final SynchronousService service;

    @Autowired
    private final AsynchronousService asynchronousService;

    public ServiceController(SynchronousService service, AsynchronousService asynchronousService) {
        this.service = service;
        this.asynchronousService = asynchronousService;
    }

    @PostMapping("/sync")
    public MilkTeaSpecResponse syncInvoke(@RequestBody MilkTeaSpecRequest request) {
        return new MilkTeaSpecResponse(service.execute(UUID.randomUUID().toString(), request));
    }

    @PostMapping("/async-sync")
    public CompletableFuture<MilkTeaSpecResponse> asyncSyncInvoke(@RequestBody MilkTeaSpecRequest request) {
        return CompletableFuture.supplyAsync(() -> new MilkTeaSpecResponse(service.execute(UUID.randomUUID().toString(), request)));
    }

    @PostMapping("/async")
    public CompletableFuture<MilkTeaSpecResponse> asyncInvoke(@RequestBody MilkTeaSpecRequest request) {
        return asynchronousService.executeAsync(UUID.randomUUID().toString(), request)
                .thenApply(milkTea -> new MilkTeaSpecResponse(milkTea.getMilkTea()));
    }
}
