package com.mrsaraira.async;

import com.mrsaraira.async.exception.AsyncExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = AsyncServiceImplTest.TestConfiguration.class)
public class AsyncServiceImplTest {

    @Autowired
    private AsyncService asyncService;

    @Test
    public void testExecuteSuppliers() {
        Supplier<String> first = () -> "Hello";
        Supplier<String> second = () -> "World";
        BiFunction<String, String, String> mergeFunction = (hello, world) -> hello + " " + world;

        String result = asyncService.execute(first, second, mergeFunction);

        Assertions.assertEquals("Hello World", result);
    }

    @Test
    public void testExecuteWithThreeSuppliers() {
        Supplier<String> first = () -> "Hello";
        Supplier<String> second = () -> "Beautiful";
        Supplier<String> third = () -> "World";
        TriFunction<String, String, String, String> mergeFunction = (a, b, c) -> a + " " + b + " " + c;

        String result = asyncService.execute(first, second, third, mergeFunction);

        Assertions.assertEquals("Hello Beautiful World", result);
    }

    @Test
    public void testExecuteRetryableNoError() {
        Supplier<String> supplier = mock(Supplier.class);
        Mockito.when(supplier.get()).thenReturn("Hello");

        String result = asyncService.execute(() -> asyncService.executeRetryable(supplier));
        Assertions.assertEquals("Hello", result);
        verify(supplier, times(1)).get();
    }

    @Test
    public void testExecuteSingleRetryableSupplierWithException() {
        Supplier<String> supplier = mock(Supplier.class);
        Mockito.when(supplier.get()).thenThrow(new RuntimeException("Error"));

        Assertions.assertThrows(AsyncExecutionException.class, () -> asyncService.executeRetryable(supplier));
        verify(supplier, times(2)).get();
    }

    @Test
    public void testExecuteWithException() {
        Supplier<String> first = () -> {
            throw new IllegalArgumentException("Error");
        };
        Supplier<String> second = () -> "World";
        BiFunction<String, String, String> mergeFunction = (obj, str) -> obj + str;

        Assertions.assertThrows(AsyncExecutionException.class, () -> {
            asyncService.execute(first, second, mergeFunction);
        });
    }

    @Test
    public void testExecuteRetryableWithException() {
        Supplier<String> errorSupplier = mock(Supplier.class);
        Mockito.when(errorSupplier.get()).thenThrow(new IllegalArgumentException("Error"));
        Supplier<String> second = () -> "World";
        BiFunction<String, String, String> mergeFunction = (first, sec) -> first + " " + sec;

        Assertions.assertThrows(AsyncExecutionException.class, () -> {
            asyncService.execute(() -> asyncService.executeRetryable(errorSupplier), second, mergeFunction);
        });

        verify(errorSupplier, times(2)).get();
    }

    @Test
    public void testExecuteRetryableWithRecovery() {
        Supplier<String> errorSupplier = mock(Supplier.class);
        Mockito.when(errorSupplier.get()).thenThrow(new IllegalStateException("Error"));
        Supplier<String> recoveryCallback = () -> "Recovery";

        String result = asyncService.executeRetryable(errorSupplier, recoveryCallback);

        Assertions.assertEquals("Recovery", result);
        verify(errorSupplier, times(2)).get();
    }

    @Test
    public void testExecuteWithThreeSuppliersWithExceptionRecovery() {
        Supplier<String> first = () -> "Hello";
        Supplier<String> errorSupplier = mock(Supplier.class);
        Mockito.when(errorSupplier.get()).thenThrow(new UnsupportedOperationException("Error"));
        Supplier<String> third = () -> "World";
        TriFunction<String, String, String, String> mergeFunction = (a, b, c) -> a + " " + b + " " + c;

        String result = asyncService.execute(
                first,
                () -> asyncService.executeRetryable(errorSupplier, () -> "Recovered"),
                third,
                mergeFunction
        );

        Assertions.assertEquals("Hello Recovered World", result);
        verify(errorSupplier, times(2)).get();
    }

    @Test
    public void testExecuteRunnables() {
        final var runnable1 = mock(Runnable.class);
        final var runnable2 = mock(Runnable.class);
        final var runnable3 = mock(Runnable.class);

        try {
            asyncService.execute(runnable1, runnable2, runnable3);

            verify(runnable1, times(1)).run();
            verify(runnable2, times(1)).run();
            verify(runnable3, times(1)).run();
        } catch (Exception e) {
            fail("Execution threw an exception", e);
        }

    }

    @ComponentScan("com.mrsaraira.async")
    @Configuration
    static class TestConfiguration {
    }

}
