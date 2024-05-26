package com.mrsaraira.async;

import com.mrsaraira.async.exception.AsyncExecutionException;
import jakarta.validation.constraints.NotNull;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Provides asynchronous execution functionality, merging results from multiple asynchronous operations, retry failed operation.
 *
 * @author Takhsin Saraira
 * @see com.mrsaraira.async.impl.AsyncServiceImpl
 */
public interface AsyncService {

    /**
     * Executes the given Supplier asynchronously.
     *
     * @param first the Supplier to execute
     * @return the result of the Supplier computation
     * @throws AsyncExecutionException if an exception occurs during the execution
     */
    <T> T execute(@NotNull Supplier<T> first) throws AsyncExecutionException;

    /**
     * Executes the given merge function with two supplier inputs and returns the result.
     *
     * @param first         The supplier for the first input
     * @param second        The supplier for the second input
     * @param mergeFunction The function that merges the two inputs and produces the result
     * @param <T>           The type of the first input
     * @param <U>           The type of the second input
     * @param <R>           The type of the result
     * @return The result of merging the two inputs
     * @throws AsyncExecutionException If an error occurs during the execution
     */
    <T, U, R> R execute(@NotNull Supplier<T> first, @NotNull Supplier<U> second, @NotNull BiFunction<T, U, R> mergeFunction) throws AsyncExecutionException;

    /**
     * Executes the given merge function with three supplier inputs and returns the result.
     *
     * @param first         The supplier for the first input
     * @param second        The supplier for the second input
     * @param third         The supplier for the third input
     * @param mergeFunction The function that merges the three inputs and produces the result
     * @param <T>           The type of the first input
     * @param <U>           The type of the second input
     * @param <V>           The type of the third input
     * @param <R>           The type of the result
     * @return The result of merging the three inputs
     * @throws AsyncExecutionException if an exception occurs during the execution
     */
    <T, U, V, R> R execute(@NotNull Supplier<T> first, @NotNull Supplier<U> second, @NotNull Supplier<V> third, @NotNull TriFunction<T, U, V, R> mergeFunction) throws AsyncExecutionException;

    /**
     * Executes the given list of Runnables asynchronously.
     *
     * @param runnables the list of Runnable to execute asynchronously
     * @throws AsyncExecutionException if an error occurs during the asynchronous execution
     */
    void execute(@NotNull Runnable... runnables) throws AsyncExecutionException;

    /**
     * Executes the given supplier with retry functionality.
     *
     * @param supplier The supplier to be executed with retry logic.
     * @param <T>      The type of the supplier.
     * @return The result of executing the supplier with retry functionality.
     * @throws AsyncExecutionException if an exception occurs during the execution
     */
    <T> T executeRetryable(@NotNull Supplier<T> supplier) throws AsyncExecutionException;

    /**
     * Executes a retryable operation using the provided supplier function and recovery callback.
     *
     * @param supplier         The supplier representing the main operation to be executed.
     * @param recoveryCallback The supplier for recovery operation in case of failure.
     * @return The result of the main operation execution.
     * @throws AsyncExecutionException if an exception occurs during the execution
     */
    <T> T executeRetryable(@NotNull Supplier<T> supplier, Supplier<T> recoveryCallback) throws AsyncExecutionException;

}
