package hu.finex.main.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.core.StreamReadConstraints;

// Alkalmazásszintű beállítások: ütemezett feladatok és az üzleti paraméterek betöltése

@Configuration
@EnableScheduling
@EnableConfigurationProperties(FinexProperties.class)
public class FinexConfig {

    // Egy JSON-szövegmező legfeljebb 20 000 karakter lehet (a Jackson alapból 20 milliót engedne)
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer requestSizeLimits() {
        return builder -> builder.postConfigurer(objectMapper ->
                objectMapper.getFactory().setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(20_000).build()));
    }
}
