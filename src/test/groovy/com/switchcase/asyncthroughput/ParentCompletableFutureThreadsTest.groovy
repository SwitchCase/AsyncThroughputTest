package com.switchcase.asyncthroughput

import com.google.common.base.Charsets
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.switchcase.asyncthroughput.futures.ParentExecutorCompletableFuture
import groovy.util.logging.Slf4j
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.RequestBuilder
import org.asynchttpclient.Response
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpResponse
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request

@Slf4j
class ParentCompletableFutureThreadsTest extends Specification {

    @Shared
    ClientAndServer mockServer
    ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("my-executor-%d").setDaemon(false).build();
    //Executor executor = Executors.newFixedThreadPool(20, factory);
    Executor executor = ForkJoinPool.commonPool()

    def asyncHttpClient = new DefaultAsyncHttpClient();
    def asyncHttpClient2 = new DefaultAsyncHttpClient();

    def setupSpec() {
        mockServer = startClientAndServer(9192);
        //create a mock server which response with "done" after 100ms.
        mockServer.when(request()
                .withMethod("POST")
                .withPath("/validate"))
                .respond(HttpResponse.response().withBody("done")
                        .withStatusCode(200)
                        .withDelay(TimeUnit.MILLISECONDS, 100));
    }

    def "Calls using one AHC with a blocking call with 1sec timeout results in TimeoutException."() {
        when:
        def value = callExternal().thenApply({ resp -> callExternalBlocking() }).toCompletableFuture().join()

        then:
        value == "done"
    }

    def "Calls using one AHC with a blocking call on ForkJoinPool with 1sec timeout results in success."() {
        when:
        def value = callExternal().thenApplyAsync({ resp -> callExternalBlocking() }).toCompletableFuture().join()

        then:
        value == "done"
    }

    def "Calls using 2 AHC with a blocking call with 1sec timeout results in success."() {
        when:
        def value = callExternal().thenApply({ resp -> callDifferentExternalBlocking() }).toCompletableFuture().join()

        then:
        value == "done"
    }

    def cleanupSpec() {
        mockServer.stop(true)
    }

    private CompletionStage<String> callExternal() {
        RequestBuilder requestBuilder = RequestBuilder.newInstance();
        requestBuilder.setMethod("POST").setUrl("http://localhost:9192/validate").setRequestTimeout(1000)
        def cf = new ParentExecutorCompletableFuture(asyncHttpClient.executeRequest(requestBuilder).toCompletableFuture(), executor);
        return cf.thenApply({ response ->
            log.info("Received response: $response")
            response.getResponseBody(Charsets.UTF_8) })
    }

    private String callExternalBlocking() {
        RequestBuilder requestBuilder = RequestBuilder.newInstance();
        requestBuilder.setMethod("POST").setUrl("http://localhost:9192/validate").setRequestTimeout(1000)
        CompletionStage<Response> cf = new ParentExecutorCompletableFuture(asyncHttpClient.executeRequest(requestBuilder).toCompletableFuture(), executor);
        return cf.thenApply({ response ->
            log.info("Received response: $response")
            response.getResponseBody(Charsets.UTF_8) }).toCompletableFuture().join()
    }

    private String callDifferentExternalBlocking() {
        RequestBuilder requestBuilder = RequestBuilder.newInstance();
        requestBuilder.setMethod("POST").setUrl("http://localhost:9192/validate").setRequestTimeout(1000)
        CompletionStage<Response> cf = new ParentExecutorCompletableFuture(asyncHttpClient2.executeRequest(requestBuilder).toCompletableFuture(), executor);
        return cf.thenApply({ response ->
            log.info("Received response: $response")
            response.getResponseBody(Charsets.UTF_8) }).toCompletableFuture().join()
    }
}