package com.lesmodromo.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS só importa quando o front chama a API direto de outra origem (no seu PC: localhost:8081 -> localhost:8080).
 * Em produção o front chama /api no próprio domínio da Vercel, que repassa para o Render, então nem passa por aqui.
 * allowCredentials(true) é o que deixa o navegador mandar o cookie de sessão junto.
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
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
