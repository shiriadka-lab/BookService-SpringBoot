package com.learn.bookService.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.cache.Cache;


import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Redis caching.
 * Sets up a RedisCacheManager with default and per-cache configurations.
 * This allows us to control how different caches behave, such as setting different TTLs for different cache names.
 * This is helpful when we have different types of data that require different caching strategies 
 * (e.g., some data might be more volatile and need shorter TTLs, while other data can be cached for longer periods).
 * This prevents stale data from being served and optimizes cache usage based on the nature of the data being cached.
 * This also corrects cache values which might be stale due to updates in the underlying data store, 
 * ensuring that users get fresh data when they access the cache.
 * 
 *  In this service
 *  ------------------------------------------------------------------
 *  Caching          ✅ @Cacheable, @CacheEvict with CacheConstants
	TTL              ✅ 5 mins for books, 10 mins default
	Serialization    ✅ BookDTO implements Serializable
	Memory limit     ✅ 256MB maxmemory + allkeys-lru eviction
	Persistence      ✅ RDB snapshot every 60s
	Error handling   ✅ CacheErrorHandler — graceful degradation to DB
	Monitoring       ✅ redis-exporter + Grafana dashboard 763
 */
@Configuration
//@Primary // Mark this as the primary CacheManager if there are multiple CacheManager beans in the context
@EnableCaching
public class RedisConfig implements CachingConfigurer {
	 private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
	 
    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    	
        logger.info("=== RedisConfig: Initializing RedisCacheManager with TTL ===");
        
        // Default config — applies to all caches unless overridden
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // default 10 min TTL
                .disableCachingNullValues();        // don't cache nulls

        // Per-cache config — override TTL per cache name
        // This allows us to have different expiration policies for different caches
        // this is useful when some data is more volatile than others (e.g., books might change more frequently than other data)
        // Here we set a specific TTL for the "books" cache, which will override the default TTL for that cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("books", RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues());  // books expire in 5 mins

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
    

    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                logger.warn("Cache GET error on cache={} key={} — falling through to DB: {}", 
                    cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                logger.warn("Cache PUT error on cache={} key={} — {}", 
                    cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                logger.warn("Cache EVICT error on cache={} key={} — {}", 
                    cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                logger.warn("Cache CLEAR error on cache={} — {}", 
                    cache.getName(), e.getMessage());
            }
        };
    }
}