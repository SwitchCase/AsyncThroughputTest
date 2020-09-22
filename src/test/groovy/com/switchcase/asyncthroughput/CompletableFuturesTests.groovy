package com.switchcase.asyncthroughput

import spock.lang.Specification

import java.util.concurrent.*

class CompletableFuturesTests extends Specification {

    def "Thread.sleep causes bottlenecking"() {
        given: "single-threaded executor"
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        when: "10 CFs with 1sec sleep is submitted"
        def cfs = []
        def start = System.currentTimeMillis();
        for(int i = 0; i < 10; i++) {
            int id = i;
            cfs.add(CompletableFuture.supplyAsync({r -> println("Start Executing $id")}, executorService)
                    .thenApplyAsync({r -> Thread.sleep(1000)}, executorService)
                    .thenApplyAsync({r -> println("Completed Executing $id")}, executorService))
        }

        CompletableFuture.allOf(cfs as CompletableFuture[]).join();

        then: "they are executed in (approx) sequential order"
        cfs.size() == 10
        def end = System.currentTimeMillis();
        end - start > 10000 //takes at least 10 seconds to complete.

        cleanup:
        executorService.shutdownNow()
    }

    def "Thread.sleep does not bottleneck when more threads are available"() {
        given: "multi-threaded executor"
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        when: "10 CFs with 1sec sleep is submitted"
        def cfs = []
        def start = System.currentTimeMillis();
        for(int i = 0; i < 10; i++) {
            int id = i;
            cfs.add(CompletableFuture.supplyAsync({r -> println("Start Executing $id")}, executorService)
                    .thenApplyAsync({r -> Thread.sleep(1000)}, executorService)
                    .thenApplyAsync({r -> println("Completed Executing $id")}, executorService))
        }

        CompletableFuture.allOf(cfs as CompletableFuture[]).join();

        then: "they are executed in (approx) sequential order"
        cfs.size() == 10
        def end = System.currentTimeMillis();
        end - start < 2000 //does not take more than 2 secs

        cleanup:
        executorService.shutdownNow()
    }

    def "CF with Rejection Policy"() {
        given: "single-threaded executor"
        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue(1), new ThreadPoolExecutor.AbortPolicy());

        when: "3 CFs with 1sec sleep is submitted"
        def cfs = []
        for(int i = 0; i < 3; i++) {
            int id = i;
            cfs.add(CompletableFuture.supplyAsync({r -> println("Start Executing $id")})
                    .thenApplyAsync({r -> Thread.sleep(1000)}, executorService)
                    .thenApplyAsync({r -> println("Completed Executing $id")}, executorService))
        }

        CompletableFuture.allOf(cfs as CompletableFuture[]).join();

        then: "RejectionException"
        CompletionException ex = thrown()
        ex.getCause().class == RejectedExecutionException.class

        cleanup:
        executorService.shutdownNow()
    }
}