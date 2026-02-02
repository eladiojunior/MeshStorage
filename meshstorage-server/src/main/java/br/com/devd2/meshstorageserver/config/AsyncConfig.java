package br.com.devd2.meshstorageserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Classe responsável por configurar as chamadas assincrinas do projeto.
 * Otimizado para melhor performance e gerenciamento de recursos.
 */
@EnableAsync
@Configuration
public class AsyncConfig {
    
    /**
     * Executor para métricas de erros.
     * Otimizado para lidar com picos de erros sem bloquear threads principais.
     */
    @Bean("metricsErrors")
    public Executor metricErrors() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(12);                    // Aumentado de 8 para 12
        executor.setQueueCapacity(500);                 // Aumentado de 200 para 500
        executor.setThreadNamePrefix("metric-errors-");
        executor.setKeepAliveSeconds(60);               // Threads ociosas vivem 60s
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Executor para métricas de tempo de resposta e contagem de requisições.
     * Otimizado para alta throughput de métricas.
     */
    @Bean("metricsResponseTimeAndRequestCount")
    public Executor metricResponseTimeAndRequestCount() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(12);                    // Aumentado de 8 para 12
        executor.setQueueCapacity(500);                 // Aumentado de 200 para 500
        executor.setThreadNamePrefix("metric-times-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Executor dedicado para operações de compressão de imagens.
     * CPU-intensive, então limitamos o número de threads ao número de cores disponíveis.
     */
    @Bean("compressionExecutor")
    public Executor compressionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors);           // Número de cores disponíveis
        executor.setMaxPoolSize(processors * 2);        // Máximo 2x o número de cores
        executor.setQueueCapacity(100);                 // Fila moderada para compressão
        executor.setThreadNamePrefix("compression-");
        executor.setKeepAliveSeconds(120);              // Threads ociosas vivem 2 minutos
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Executor para operações de I/O assíncronas (leitura/escrita de arquivos).
     * I/O-bound, então podemos ter mais threads que cores.
     */
    @Bean("fileIOExecutor")
    public Executor fileIOExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors * 2);       // I/O pode ter mais threads
        executor.setMaxPoolSize(processors * 4);        // Até 4x o número de cores
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("file-io-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Executor padrão para operações assíncronas gerais.
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors * 3);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("async-task-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
