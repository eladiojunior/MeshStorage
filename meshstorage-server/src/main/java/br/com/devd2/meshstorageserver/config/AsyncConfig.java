package br.com.devd2.meshstorageserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Classe responsável por configurar as chamadas assincrinas do projeto.
 */
@EnableAsync
@Configuration
public class AsyncConfig {
    
    @Bean("metricsErrors")
    public Executor metricErrors() {
        return new ThreadPoolTaskExecutor() {{
            setCorePoolSize(4);
            setMaxPoolSize(8);
            setQueueCapacity(200);
            setThreadNamePrefix("metric-errors-");
            initialize();
        }};
    }

    @Bean("metricsResponseTimeAndRequestCount")
    public Executor metricResponseTimeAndRequestCount() {
        return new ThreadPoolTaskExecutor() {{
            setCorePoolSize(4);
            setMaxPoolSize(8);
            setQueueCapacity(200);
            setThreadNamePrefix("metric-times-");
            initialize();
        }};
    }
}
