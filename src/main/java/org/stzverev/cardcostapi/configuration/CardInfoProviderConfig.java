package org.stzverev.cardcostapi.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.stzverev.cardcostapi.configuration.properties.IINInfoCacheConfig;
import org.stzverev.cardcostapi.domain.repository.IINCacheRepository;
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
    public IINInfoProvider cardInfoProvider(@Autowired IINInfoProviderBinList cardInfoProviderBinList,
                                            @Autowired IINCacheRepository iinCacheRepository,
                                            @Autowired IINExtractor iinExtractor) {
        log.info("Cache is registred");
        log.info("Cache timeUnit: {}", cacheConfig.getTimeUnit());
        return new IINCacheProvider(cardInfoProviderBinList, iinCacheRepository, iinExtractor,
                Duration.of(cacheConfig.getPeriod(), cacheConfig.getTimeUnit().toChronoUnit()));
    }

    @Bean
    public IINInfoProviderBinList cardInfoProviderBinList(
            @Value("${app.thirdrpovider.binlist.baseUrl}") String binListBaseUrl,
            @Autowired IINExtractor iinExtractor) {
        log.info("IINInfoProviderBinList is registred");
        return new IINInfoProviderBinList(binListBaseUrl, iinExtractor);
    }

}
