package com.switchcase.asyncthroughput

import com.google.common.base.Charsets
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.RequestBuilder
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpResponse
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request

class CompletableFutureThreadsTest extends Specification {

    @Shared
    ClientAndServer mockServer

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
        callExternal().thenApply({ resp -> callExternalBlocking() }).join()

        then:
        def exception = thrown(CompletionException)
        exception instanceof CompletionException
        exception.getCause() instanceof TimeoutException
    }

    def "Calls using one AHC with a blocking call on ForkJoinPool with 1sec timeout results in success."() {
        when:
        def value = callExternal().thenApplyAsync({ resp -> callExternalBlocking() }).join()

        then:
        value == "done"
    }

    def "Calls using one AHC with a blocking call on a dedicated threadpool with 1sec timeout results in success."() {
        when:
        ExecutorService executorService = Executors.newFixedThreadPool(1)
        def value = callExternal().thenApplyAsync({ resp -> callExternalBlocking() }, executorService).join()

        then:
        value == "done"
    }

    def "Multiple Calls using one AHC with a blocking call on a dedicated threadpool with 1sec timeout results in success."() {
        when:
        ExecutorService executorService = Executors.newFixedThreadPool(1)
        def value = CompletableFuture.allOf(callExternal().thenApplyAsync({ resp -> callExternalBlocking() }, executorService),
                callExternal().thenApplyAsync({ resp -> callExternalBlocking() }, executorService),
                callExternal().thenApplyAsync({ resp -> callExternalBlocking() }, executorService),
                callExternal().thenApplyAsync({ resp -> callExternalBlocking() }, executorService),
                callExternal().thenApplyAsync({ resp -> callExternalBlocking() }, executorService),
                callExternal().thenApplyAsync({ resp -> callExternalBlocking() }, executorService))

        then:
        noExceptionThrown()
    }

    def "Calls using 2 AHC with a blocking call with 1sec timeout results in success."() {
        when:
        def value = callExternal().thenApply({ resp -> callDifferentExternalBlocking() }).join()

        then:
        value == "done"
    }

    def cleanupSpec() {
        mockServer.stop(true)
    }

    private CompletableFuture<String> callExternal() {
        RequestBuilder requestBuilder = RequestBuilder.newInstance();
        requestBuilder.setMethod("POST").setUrl("http://localhost:9192/validate").setRequestTimeout(1000)
        def cf = asyncHttpClient.executeRequest(requestBuilder).toCompletableFuture()
        return cf.thenApply({ response -> response.getResponseBody(Charsets.UTF_8) })
    }

    private String callExternalBlocking() {
        RequestBuilder requestBuilder = RequestBuilder.newInstance();
        requestBuilder.setMethod("POST").setUrl("http://localhost:9192/validate").setRequestTimeout(1000)
        def cf = asyncHttpClient.executeRequest(requestBuilder).toCompletableFuture()
        return cf.thenApply({ response -> response.getResponseBody(Charsets.UTF_8) }).join()
    }

    private String callDifferentExternalBlocking() {
        RequestBuilder requestBuilder = RequestBuilder.newInstance();
        requestBuilder.setMethod("POST").setUrl("http://localhost:9192/validate").setRequestTimeout(1000)
        def cf = asyncHttpClient2.executeRequest(requestBuilder).toCompletableFuture()
        return cf.thenApply({ response -> response.getResponseBody(Charsets.UTF_8) }).join()
    }
}