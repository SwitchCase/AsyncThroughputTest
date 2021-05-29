package com.switchcase.asyncthroughput.futures;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This is an extension on the CompletableFuture. It prevents usage of implicit ForkJoinPools or letting the VM
 * figure out where the function is executed.
 *
 * In this CF, we force the creator to always provide the executor (even if it is the {@link ForkJoinPool#commonPool()}).
 * All 'child' executions/callbacks will always be executed on the parent's executor (not necessarily threads) or on
 * the executor that was explicitly provided (as part of then*Async).
 * @param <T>
 */
public class ParentExecutorCompletableFuture<T> implements CompletionStage<T> {

    private final CompletableFuture<T> baseFuture;
    private final Executor executor;

    public static<T> ParentExecutorCompletableFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        return new ParentExecutorCompletableFuture(supplier, executor);
    }

    public ParentExecutorCompletableFuture(Supplier<T> supplier, Executor executor) {
        this.baseFuture = CompletableFuture.supplyAsync(supplier, executor);
        this.executor = executor;
    }

    public ParentExecutorCompletableFuture(CompletableFuture<T> baseFuture,
                                            Executor executor) {
        this.baseFuture = baseFuture;
        this.executor = executor;
    }

    @Override
    public <U> CompletionStage<U> thenApply(Function<? super T, ? extends U> fn) {
        return thenApplyAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return thenApplyAsync(fn, executor);
    }

    @Override
    public <U> ParentExecutorCompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn,
                                                                 Executor executor) {
        CompletableFuture<U> newBase = baseFuture.thenApplyAsync(fn, executor);
        return new ParentExecutorCompletableFuture<>(newBase, executor);
    }

    @Override
    public <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return thenComposeAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return thenComposeAsync(fn, executor);
    }

    @Override
    public <U> ParentExecutorCompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
                                                                   Executor executor) {
        CompletableFuture<U> newBase = baseFuture.thenComposeAsync(fn, executor);
        return new ParentExecutorCompletableFuture<>(newBase, executor);
    }

    @Override
    public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return new ParentExecutorCompletableFuture<>(baseFuture.exceptionally(fn), executor);
    }

    @Override
    public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return whenCompleteAsync(action);
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return whenCompleteAsync(action, executor);
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.whenCompleteAsync(action, executor), executor);
    }

    @Override
    public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return handleAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return handleAsync(fn, executor);
    }

    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.handleAsync(fn, executor), executor);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return baseFuture;
    }

    @Override
    public CompletionStage<Void> thenAccept(Consumer<? super T> action) {
        return thenAcceptAsync(action);
    }

    @Override
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action) {
        return thenAcceptAsync(action, executor);
    }

    @Override
    public ParentExecutorCompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action,
                                                   Executor executor) {
        CompletableFuture<Void> newBase = baseFuture.thenAcceptAsync(action, executor);
        return new ParentExecutorCompletableFuture<>(newBase, executor);
    }

    @Override
    public CompletionStage<Void> thenRun(Runnable action) {
        return thenRunAsync(action);
    }

    @Override
    public CompletionStage<Void> thenRunAsync(Runnable action) {
        return thenRunAsync(action, executor);
    }

    @Override
    public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.thenRunAsync(action, executor), executor);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return thenCombineAsync(other, fn);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return baseFuture.thenCombineAsync(other, fn, executor);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.thenCombineAsync(other, fn, executor), executor);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return thenAcceptBothAsync(other, action);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return thenAcceptBothAsync(other, action, executor);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.thenAcceptBothAsync(other, action, executor), executor);
    }

    @Override
    public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return runAfterBothAsync(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return runAfterBothAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.runAfterBothAsync(other, action, executor), executor);
    }

    @Override
    public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return applyToEitherAsync(other, fn);
    }

    @Override
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return applyToEitherAsync(other, fn, executor);
    }

    @Override
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.applyToEitherAsync(other, fn, executor), executor);
    }

    @Override
    public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return acceptEitherAsync(other, action);
    }

    @Override
    public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return acceptEitherAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.acceptEitherAsync(other, action, executor), executor);
    }

    @Override
    public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return runAfterEitherAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return runAfterEitherAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return new ParentExecutorCompletableFuture<>(baseFuture.runAfterEitherAsync(other, action, executor), executor);
    }

    public T join() {
        return baseFuture.join();
    }

    /**
     * Returns {@code true} if completed in any fashion: normally,
     * exceptionally, or via cancellation.
     *
     * @return {@code true} if completed
     */
    public boolean isDone() {
       return baseFuture.isDone();
    }

    public boolean complete(T value) {
        return baseFuture.complete(value);
    }

    /**
     * If not already completed, causes invocations of {@link #get()}
     * and related methods to throw the given exception.
     *
     * @param ex the exception
     * @return {@code true} if this invocation caused this CompletableFuture
     * to transition to a completed state, else {@code false}
     */
    public boolean completeExceptionally(Throwable ex) {
        return baseFuture.completeExceptionally(ex);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return baseFuture.cancel(mayInterruptIfRunning);
    }

    /**
     * Returns {@code true} if this CompletableFuture was cancelled
     * before it completed normally.
     *
     * @return {@code true} if this CompletableFuture was cancelled
     * before it completed normally
     */
    public boolean isCancelled() {
        return baseFuture.isCancelled();
    }

    /**
     * Returns {@code true} if this CompletableFuture completed
     * exceptionally, in any way. Possible causes include
     * cancellation, explicit invocation of {@code
     * completeExceptionally}, and abrupt termination of a
     * CompletionStage action.
     *
     * @return {@code true} if this CompletableFuture completed
     * exceptionally
     */
    public boolean isCompletedExceptionally() {
        return baseFuture.isCompletedExceptionally();
    }

    /**
     * Forcibly sets or resets the value subsequently returned by
     * method {@link #get()} and related methods, whether or not
     * already completed. This method is designed for use only in
     * error recovery actions, and even in such situations may result
     * in ongoing dependent completions using established versus
     * overwritten outcomes.
     *
     * @param value the completion value
     */
    public void obtrudeValue(T value) {
        baseFuture.obtrudeValue(value);
    }

    /**
     * Forcibly causes subsequent invocations of method {@link #get()}
     * and related methods to throw the given exception, whether or
     * not already completed. This method is designed for use only in
     * error recovery actions, and even in such situations may result
     * in ongoing dependent completions using established versus
     * overwritten outcomes.
     *
     * @param ex the exception
     * @throws NullPointerException if the exception is null
     */
    public void obtrudeException(Throwable ex) {
        baseFuture.obtrudeException(ex);
    }

    /**
     * Returns the estimated number of CompletableFutures whose
     * completions are awaiting completion of this CompletableFuture.
     * This method is designed for use in monitoring system state, not
     * for synchronization control.
     *
     * @return the number of dependent CompletableFutures
     */
    public int getNumberOfDependents() {
       return baseFuture.getNumberOfDependents();
    }

}
