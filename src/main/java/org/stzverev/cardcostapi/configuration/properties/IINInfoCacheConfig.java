package org.stzverev.cardcostapi.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties("app.iin-cache")
@Data
public class IINInfoCacheConfig {

    private TimeUnit timeUnit = TimeUnit.DAYS;

    private Long period = 1L;

    private boolean enabled;

}
