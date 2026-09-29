package hu.finex.main.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

// Alkalmazásszintű beállítások:
// - ütemezett feladatok (havi kamatjóváírás, rendszeres átutalások)
// - az üzleti paraméterek betöltése (FinexProperties)
// - a lapozott válaszok stabil JSON szerkezete: { "content": [...], "page": { "size", "number", "totalElements", "totalPages" } }

@Configuration
@EnableScheduling
@EnableConfigurationProperties(FinexProperties.class)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class FinexConfig {
}
