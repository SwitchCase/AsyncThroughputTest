package com.switchcase.asyncthroughput

import com.google.common.util.concurrent.ThreadFactoryBuilder
import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.util.concurrent.*

@Slf4j
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

    def "CF execution contexts explicit ThreadPool with 'Async' APIs"() {
        given: "single-threaded executors"
        ThreadPoolExecutor executorService0 = Spy(ThreadPoolExecutor, constructorArgs:[1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue(5), getThreadFactory("exec-zero")]);
        ThreadPoolExecutor executorService1 = Spy(ThreadPoolExecutor, constructorArgs:[1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue(5), getThreadFactory("exec-one")]);

        when: "executed"
        CompletableFuture.supplyAsync({r -> logAndIncrementNumber(1)}, executorService0)
                .thenApplyAsync({r -> logAndIncrementNumber(r)}, executorService0)
                .thenApplyAsync({r -> logAndIncrementNumber(r)}, executorService0)
                .thenApplyAsync({r -> logAndIncrementNumber(r)}, executorService1)
                .thenApplyAsync({r -> logAndIncrementNumber(r)}, executorService1)
                .thenApplyAsync({r -> logAndIncrementNumber(r)}, executorService1).join();

        then:
        noExceptionThrown()
        3 * executorService0.execute(_)
        3 * executorService1.execute(_)
    }

    def "CF execution without explicit ThreadPool"() {
        given: "single-threaded executors"
        ThreadPoolExecutor executorService0 = Spy(ThreadPoolExecutor, constructorArgs:[1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue(5), getThreadFactory("exec-zero")]);
        ThreadPoolExecutor executorService1 = Spy(ThreadPoolExecutor, constructorArgs:[1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue(5), getThreadFactory("exec-one")]);

        when: "executed"
        CompletableFuture.supplyAsync({r -> logAndIncrementNumber(1)}, executorService0)
                .thenApply({r -> logAndIncrementNumber(r)})
                .thenApply({r -> logAndIncrementNumber(r)})
                .thenApplyAsync({r -> logAndIncrementNumber(r)}, executorService1)
                .thenApply({r -> logAndIncrementNumber(r)})
                .thenApply({r -> logAndIncrementNumber(r)}).join();

        then:
        noExceptionThrown()
        1 * executorService0.execute(_)
        1 * executorService1.execute(_)
    }

    private int logAndIncrementNumber(int v) {
        log.info("Print {}", v)
        return v + 1;
    }

    ThreadFactory getThreadFactory(String prefix) {
        return new ThreadFactoryBuilder().setNameFormat(prefix + "-%d").build();
    }
}
