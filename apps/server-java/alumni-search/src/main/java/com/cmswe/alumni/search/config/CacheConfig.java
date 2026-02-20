package com.cmswe.alumni.search.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类 - 使用 Caffeine 实现本地缓存
 *
 * @author CNI Alumni System
 */
@Configuration
public class CacheConfig {

    /**
     * 搜索结果缓存（L1 本地缓存）
     * TTL: 5分钟
     * 最大容量: 1000条
     */
    @Bean("searchResultCache")
    public Cache<String, Object> searchResultCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats()
                .build();
    }

    /**
     * 热搜榜缓存
     * TTL: 5分钟
     * 最大容量: 100条
     */
    @Bean("hotSearchCache")
    public Cache<String, Object> hotSearchCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .recordStats()
                .build();
    }

    /**
     * 搜索建议缓存
     * TTL: 30分钟
     * 最大容量: 500条
     */
    @Bean("suggestCache")
    public Cache<String, Object> suggestCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()
                .build();
    }
}
