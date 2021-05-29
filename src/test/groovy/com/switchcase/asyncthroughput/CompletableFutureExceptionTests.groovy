package com.switchcase.asyncthroughput

import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

@Slf4j
class CompletableFutureExceptionTests extends Specification {

    def "CF with exceptionally - exceptionally is called at first RTE skipping over other applies"() {
        when:
        def future = CompletableFuture.supplyAsync({ -> "start" })
                .thenApply({ str -> logAndReturn(str + " apply - 1") })
                .thenApplyAsync({ str -> throw new RuntimeException("mock")})
                .thenApply({ str -> logAndReturn(str + " apply - 2") })
                .exceptionally({ex ->
                    log.error("Exception ex.", ex);
                    throw (RuntimeException) ex;
                })
                .thenApply({ str -> logAndReturn(str + " apply - 3") }).join()

        then:
        def exception = thrown(CompletionException)
        exception.getCause().getMessage() == "mock"
    }

    def "CF with double exceptionally - exceptionally is called at first RTE skipping over other applies"() {
        when:
        def future = CompletableFuture.supplyAsync({ -> "start" })
                .thenApply({ str -> logAndReturn(str + " apply - 1") })
                .thenApplyAsync({ str -> throw new RuntimeException("mock exception")})
                .exceptionally{ex -> ex.getMessage() }
                .thenApply({ str -> logAndReturn(str + " apply - 2") })
                .thenApplyAsync({ str -> throw new RuntimeException("mock exception 2")})
                .exceptionally({ex ->
                    log.error("Exception ex.", ex);
                    throw (RuntimeException) ex;
                })
                .thenApply({ str -> logAndReturn(str + " apply - 3") }).join()

        then:
        def exception = thrown(CompletionException)
        exception.getCause().getMessage() == "mock exception 2"
    }

    def logAndReturn(String str) {
        log.info(str)
        return str;
    }
}