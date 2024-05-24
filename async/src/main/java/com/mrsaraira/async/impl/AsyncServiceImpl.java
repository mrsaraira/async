package com.mrsaraira.async.impl;

import com.mrsaraira.async.AsyncService;
import com.mrsaraira.async.TriFunction;
import com.mrsaraira.async.config.AsyncServiceConfiguration;
import com.mrsaraira.async.exception.AsyncExecutionException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public final class AsyncServiceImpl implements AsyncService {

    private final Executor taskExecutor;
    private final RetryTemplate retryTemplate;

    @Override
    public <T> T execute(@NonNull Supplier<T> first) throws AsyncExecutionException {
        final var future = CompletableFuture.supplyAsync(first, taskExecutor);

        try {
            return future.get();
        } catch (Exception e) {
            throw new AsyncExecutionException(e);
        }
    }

    @Override
    public <T, U, R> R execute(@NonNull Supplier<T> first, @NonNull Supplier<U> second, @NonNull BiFunction<T, U, R> mergeFunction) throws AsyncExecutionException {
        final var future1 = CompletableFuture.supplyAsync(first, taskExecutor);
        final var future2 = CompletableFuture.supplyAsync(second, taskExecutor);

        try {
            return mergeFunction.apply(future1.get(), future2.get());
        } catch (Exception e) {
            throw new AsyncExecutionException(e);
        }
    }

    @Override
    public <T, U, V, R> R execute(@NonNull Supplier<T> first, @NonNull Supplier<U> second, @NonNull Supplier<V> third, @NonNull TriFunction<T, U, V, R> mergeFunction) throws AsyncExecutionException {
        final var future1 = CompletableFuture.supplyAsync(first, taskExecutor);
        final var future2 = CompletableFuture.supplyAsync(second, taskExecutor);
        final var future3 = CompletableFuture.supplyAsync(third, taskExecutor);

        final var allOf = CompletableFuture.allOf(future1, future2, future3);

        try {
            allOf.get();
            return mergeFunction.apply(future1.get(), future2.get(), future3.get());
        } catch (Exception e) {
            throw new AsyncExecutionException(e);
        }
    }

    @Override
    public <T> T executeRetryable(@NonNull Supplier<T> supplier) {
        return executeRetryable(supplier, null);
    }

    @Override
    public <T> T executeRetryable(@NonNull Supplier<T> supplier, Supplier<T> recoveryCallback) {
        try {
            return retryTemplate.execute(context -> {
                context.computeAttribute(AsyncServiceConfiguration.CONTEXT_ATTRIBUTE_ID, e -> UUID.randomUUID());
                return supplier.get();
            }, recoveryCallback == null
                    ? null
                    : context -> recoveryCallback.get());
        } catch (Exception e) {
            throw new AsyncExecutionException(e);
        }
    }

}
