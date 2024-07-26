package org.stzverev.cardcostapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.stzverev.cardcostapi.util.resourcereader.ResourceReader;

@TestConfiguration
public class TestUtilConfig {

    @Bean
    public ResourceReader resourceReader(@Autowired ObjectMapper objectMapper) {
        return new ResourceReader(objectMapper);
    }

}
