package com.switchcase.asyncthroughput.config;

import com.switchcase.asyncthroughput.controller.AsyncServiceController;
import com.switchcase.asyncthroughput.controller.ServiceController;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(ServiceController.class);
        register(AsyncServiceController.class);
    }
}
