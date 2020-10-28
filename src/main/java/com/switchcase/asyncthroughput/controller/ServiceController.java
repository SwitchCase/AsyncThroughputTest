package com.switchcase.asyncthroughput.controller;

import com.switchcase.asyncthroughput.SynchronousService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.server.ManagedAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Path("/")
public class ServiceController {

    @Autowired
    private SynchronousService service;

    @Qualifier("syncExec")
    @Autowired
    private ExecutorService executorService;

    @POST
    @Path("/sync")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public MilkTeaSpecResponse syncInvoke(MilkTeaSpecRequest request) {
        return new MilkTeaSpecResponse(service.execute(UUID.randomUUID().toString(), request));
    }

    @POST
    @Path("/async-sync")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void asyncSyncInvoke(@Suspended AsyncResponse asyncResponse, MilkTeaSpecRequest request) {
        CompletableFuture.supplyAsync(() -> new MilkTeaSpecResponse(service.execute(UUID.randomUUID().toString(), request)), executorService)
            .thenApply(asyncResponse::resume);
    }

}
