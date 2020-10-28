package com.switchcase.asyncthroughput.controller;

import com.switchcase.asyncthroughput.AsynchronousServicev2;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.server.ManagedAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/")
public class AsyncServiceController {

    @Autowired
    private AsynchronousServicev2 asynchronousService;

    public AsyncServiceController(AsynchronousServicev2 asynchronousService) {
        this.asynchronousService = asynchronousService;
    }

    @POST
    @Path("/async")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void asyncInvoke(@Suspended AsyncResponse asyncResponse, MilkTeaSpecRequest request) {
        asynchronousService.executeAsync(UUID.randomUUID().toString(), request)
            .thenApply(milkTea -> new MilkTeaSpecResponse(milkTea.getMilkTea()))
            .thenAccept(asyncResponse::resume);
    }

}
