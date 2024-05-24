## Async Service

This library provides a handy service for asynchronous execution framework using Java's CompletableFuture and Spring's RetryTemplate. The main component of this library is the AsyncServiceImpl class, which allows for executing tasks asynchronously, with optional retry capabilities.
Overview
The AsyncServiceImpl class implements the AsyncService interface and provides methods to execute tasks asynchronously. It supports executing single, dual, and triple supplier tasks with merge functions. Additionally, it offers retryable execution using Spring's RetryTemplate.

### Features
Asynchronous Execution: Execute tasks asynchronously using CompletableFuture.
Combining Results: Combine results from multiple asynchronous tasks using BiFunction and TriFunction.
Retry Capability: Retry tasks using Spring's RetryTemplate with optional recovery callbacks.

### Prerequisites
Java 21 or above

### Installation
To include this library in your project, add the following dependency to your pom.xml if you're using Maven:
```
<dependency>
    <groupId>com.mrsaraira</groupId>
    <artifactId>async-service</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Usage

#### Basic Asynchronous Execution

##### Single Supplier Execution

```java
AsyncService asyncService = new AsyncServiceImpl(taskExecutor, retryTemplate);
Supplier<String> task = () -> "Hello, World!";

String result = asyncService.execute(task);
System.out.println(result); // Output: Hello, World!

```

##### Dual Supplier Execution with Merge Function

```java
Supplier<Integer> task1 = () -> 10;
Supplier<Integer> task2 = () -> 20;
BiFunction<Integer, Integer, Integer> addFunction = Integer::sum;

Integer result = asyncService.execute(task1, task2, addFunction);
System.out.println(result); // Output: 30

```

##### Triple Supplier Execution with Merge Function

```java
Supplier<Integer> task1 = () -> 1;
Supplier<Integer> task2 = () -> 2;
Supplier<Integer> task3 = () -> 3;
TriFunction<Integer, Integer, Integer, Integer> sumFunction = (a, b, c) -> a + b + c;

Integer result = asyncService.execute(task1, task2, task3, sumFunction);
System.out.println(result); // Output: 6

```

##### Retryable Execution

```java
Supplier<String> retryableTask = () -> {
    // Task that might fail
    if (Math.random() > 0.5) throw new RuntimeException("Failed!");
    return "Success!";
};


String result = asyncService.executeRetryable(retryableTask);
System.out.println(result); // might fail here

// With recovery callback
Supplier<String> recoveryTask = () -> "Recovered!";

String result = asyncService.executeRetryable(retryableTask, recoveryTask);
System.out.println(result); // Output: Recovered if retryableTask fail x times

```

### Exception Handling
All asynchronous methods throw `AsyncExecutionException` if any exception occurs during task execution. It wraps the actual exception for easier handling.

