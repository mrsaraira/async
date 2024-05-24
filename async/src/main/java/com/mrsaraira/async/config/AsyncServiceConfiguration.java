package com.mrsaraira.async.config;

import com.mrsaraira.async.AsyncService;
import com.mrsaraira.async.impl.AsyncServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncServiceConfiguration {

    public static final String CONTEXT_ATTRIBUTE_ID = "id";

    @Bean
    RetryTemplate retryTemplate() {
        return new RetryTemplateBuilder()
                .maxAttempts(2)
                .withListener(new AsyncRetryListener())
                .fixedBackoff(1000)
                .build();
    }

    @Bean
    Executor taskExecutor() {
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setMaxPoolSize(15);
        taskExecutor.setQueueCapacity(20);
        taskExecutor.initialize();

        return taskExecutor;
    }

    @Bean
    AsyncService asyncService(Executor taskExecutor, RetryTemplate retryTemplate) {
        return new AsyncServiceImpl(taskExecutor, retryTemplate);
    }

    @Slf4j
    private static class AsyncRetryListener implements RetryListener {

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            log.error("Error occurred while running async logic with uuid: {} context: {} due to: {}",
                    context.getAttribute(CONTEXT_ATTRIBUTE_ID), context, ExceptionUtils.getStackTrace(throwable)
            );
        }

    }

}
