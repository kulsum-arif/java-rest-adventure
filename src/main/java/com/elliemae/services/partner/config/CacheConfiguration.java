package com.synkrato.services.partner.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cache configuration */
@Configuration
public class CacheConfiguration {

  @Value("${synkrato.epc.cache.webhook-token-cache.name}")
  private String webhookTokenCacheName;

  @Value("${synkrato.epc.cache.webhook-token-cache.caffeine-spec}")
  private String webhookTokenCacheCaffeineSpec;

  @Value("${synkrato.epc.cache.schema-cache.name}")
  private String schemaCacheName;

  @Value("${synkrato.epc.cache.schema-cache.caffeine-spec}")
  private String schemaCacheCaffeineSpec;

  @Bean
  public CacheManager cacheManager() {
    CaffeineCache webhookTokenCache =
        new CaffeineCache(
            webhookTokenCacheName, Caffeine.from(webhookTokenCacheCaffeineSpec).build());

    CaffeineCache schemaCache =
        new CaffeineCache(schemaCacheName, Caffeine.from(schemaCacheCaffeineSpec).build());

    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(webhookTokenCache, schemaCache));
    return cacheManager;
  }
}
