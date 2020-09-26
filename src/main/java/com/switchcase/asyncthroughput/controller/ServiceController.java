package com.switchcase.asyncthroughput.controller;

import com.switchcase.asyncthroughput.AsynchronousService;
import com.switchcase.asyncthroughput.SynchronousService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
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

    @GetMapping("/test")
    public @ResponseBody CompletableFuture<String> test() throws InterruptedException {
        CompletableFuture<Boolean> boolean1= asynchronousService.veryLongMethod();
        CompletableFuture<Boolean> boolean2= asynchronousService.veryLongMethod();
        CompletableFuture<Boolean> boolean3= asynchronousService.veryLongMethod();

        return CompletableFuture.allOf(boolean1,boolean2,boolean3).thenApply(r -> "done");
    }

}
