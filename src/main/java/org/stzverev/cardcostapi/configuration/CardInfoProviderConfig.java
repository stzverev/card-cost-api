package org.stzverev.cardcostapi.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.stzverev.cardcostapi.configuration.properties.IINInfoCacheConfig;
import org.stzverev.cardcostapi.domain.entity.IINCacheEntity;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINExtractor;
import org.stzverev.cardcostapi.service.cardinfoprovider.IINInfoProvider;
import org.stzverev.cardcostapi.service.cardinfoprovider.binlist.IINInfoProviderBinList;
import org.stzverev.cardcostapi.service.cardinfoprovider.cache.IINCacheProvider;

import java.time.Duration;

@Configuration
@Slf4j
public class CardInfoProviderConfig {

    @Autowired
    private IINInfoCacheConfig cacheConfig;

    @Bean
    @Primary
    @ConditionalOnProperty("app.iin-cache.enabled")
    public IINInfoProvider cardInfoProvider(
            @Autowired @Qualifier("cardInfoProviderBinList") IINInfoProvider cardInfoProviderBinList,
            @Autowired IINExtractor iinExtractor,
            @Autowired ReactiveRedisOperations<String, IINCacheEntity> redisOperationsIINCache) {
        log.info("Cache is registred");
        log.info("Cache timeUnit: {}", cacheConfig.getTimeUnit());
        return new IINCacheProvider(cardInfoProviderBinList, iinExtractor,
                Duration.of(cacheConfig.getPeriod(), cacheConfig.getTimeUnit().toChronoUnit()),
                redisOperationsIINCache);
    }

    @Bean
    public IINInfoProvider cardInfoProviderBinList(
            @Value("${app.thirdrpovider.binlist.baseUrl}") String binListBaseUrl,
            @Autowired IINExtractor iinExtractor,
            @Autowired ReactiveRedisOperations<String, Long> apiCallCounterRedisOperations) {
        log.info("IINInfoProviderBinList is registred");
        return new IINInfoProviderBinList(binListBaseUrl, iinExtractor, apiCallCounterRedisOperations);
    }

    @Bean
    ReactiveRedisOperations<String, Long> reactiveRedisOperationsApiCallsCounter(
            ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, Long> context = RedisSerializationContext
                .<String, Long>newSerializationContext(RedisSerializer.string())
                .value(new GenericToStringSerializer<>(Long.class))
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    ReactiveRedisOperations<String, IINCacheEntity> redisOperationsIINCache(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext
                .<String, IINCacheEntity>newSerializationContext(RedisSerializer.string())
                .value(new Jackson2JsonRedisSerializer<>(IINCacheEntity.class))
                .build());
    }

}
