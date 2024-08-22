package org.stzverev.cardcostapi.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties("app.thirdrpovider.binlist")
@Data
public class BinListConfig {

    private String baseUrl;

    @NestedConfigurationProperty
    private MaxCallConfig maxCall;

    @Data
    public static class MaxCallConfig {

        private Integer count;

        private TimeUnit timeUnit;

        private Long period;

    }

}
