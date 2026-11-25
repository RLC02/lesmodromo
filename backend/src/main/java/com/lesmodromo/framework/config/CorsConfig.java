package com.lesmodromo.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Libera o front da Vercel para chamar a API. A origem vem da variável FRONTEND_URL (padrão: qualquer uma).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origens;

    public CorsConfig(@Value("${lesmodromo.cors-origens}") String origens) {
        this.origens = origens.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(origens)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}
