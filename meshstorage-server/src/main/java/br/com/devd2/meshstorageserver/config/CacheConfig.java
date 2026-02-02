package br.com.devd2.meshstorageserver.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache para melhorar performance de consultas frequentes.
 * Utiliza Caffeine como provider de cache em memória.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura o gerenciador de cache com Caffeine.
     * Define políticas de expiração e tamanho máximo para cada cache.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "fileStorageCache",
                "applicationCache",
                "storageClientCache",
                "scoreCalculationCache",
                "fileContentTypeCache",
                "duplicateHashCache"
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configuração padrão do Caffeine para todos os caches.
     * - Máximo de 1000 entradas por cache
     * - Expiração após 10 minutos sem acesso
     * - Expiração após 30 minutos desde a escrita
     * - Registro de estatísticas para monitoramento
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * Cache específico para cálculo de score com TTL menor.
     * Score muda frequentemente, então expira mais rápido.
     */
    @Bean
    public Caffeine<Object, Object> scoreCalculationCaffeine() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * Cache para verificação de hash duplicado.
     * Mantém por mais tempo pois hash não muda.
     */
    @Bean
    public Caffeine<Object, Object> duplicateHashCaffeine() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats();
    }
}
