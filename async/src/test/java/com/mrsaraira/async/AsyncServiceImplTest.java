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

@SpringBootTest(classes = AsyncServiceImplTest.TestConfiguration.class)
public class AsyncServiceImplTest {

    @Autowired
    private AsyncService asyncService;

    @Test
    public void testExecute() {
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
        Supplier<String> supplier = Mockito.mock(Supplier.class);
        Mockito.when(supplier.get()).thenReturn("Hello");

        String result = asyncService.execute(() -> asyncService.executeRetryable(supplier));
        Assertions.assertEquals("Hello", result);
        Mockito.verify(supplier, Mockito.times(1)).get();
    }

    @Test
    public void testExecuteSingleRetryableSupplierWithException() {
        Supplier<String> supplier = Mockito.mock(Supplier.class);
        Mockito.when(supplier.get()).thenThrow(new RuntimeException("Error"));

        Assertions.assertThrows(AsyncExecutionException.class, () -> asyncService.executeRetryable(supplier));
        Mockito.verify(supplier, Mockito.times(2)).get();
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
        Supplier<String> errorSupplier = Mockito.mock(Supplier.class);
        Mockito.when(errorSupplier.get()).thenThrow(new IllegalArgumentException("Error"));
        Supplier<String> second = () -> "World";
        BiFunction<String, String, String> mergeFunction = (first, sec) -> first + " " + sec;

        Assertions.assertThrows(AsyncExecutionException.class, () -> {
            asyncService.execute(() -> asyncService.executeRetryable(errorSupplier), second, mergeFunction);
        });

        Mockito.verify(errorSupplier, Mockito.times(2)).get();
    }

    @Test
    public void testExecuteRetryableWithRecovery() {
        Supplier<String> errorSupplier = Mockito.mock(Supplier.class);
        Mockito.when(errorSupplier.get()).thenThrow(new IllegalStateException("Error"));
        Supplier<String> recoveryCallback = () -> "Recovery";

        String result = asyncService.executeRetryable(errorSupplier, recoveryCallback);

        Assertions.assertEquals("Recovery", result);
        Mockito.verify(errorSupplier, Mockito.times(2)).get();
    }

    @Test
    public void testExecuteWithThreeSuppliersWithExceptionRecovery() {
        Supplier<String> first = () -> "Hello";
        Supplier<String> errorSupplier = Mockito.mock(Supplier.class);
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
        Mockito.verify(errorSupplier, Mockito.times(2)).get();
    }

    @ComponentScan("com.mrsaraira.async")
    @Configuration
    static class TestConfiguration {
    }

}
