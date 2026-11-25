package com.lesmodromo.framework.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Origens que podem chamar a API, vindas da variável FRONTEND_URL (separadas por vírgula).
 *  - No seu PC: localhost:8081 chamando localhost:8080 (padrão do application.yml).
 *  - Em produção: mesmo passando pelo proxy da Vercel, o navegador manda o cabeçalho Origin
 *    nos POST (login, cadastro, largar corrida) e a Vercel repassa ele ao Render.
 *    Por isso a URL da Vercel precisa estar no FRONTEND_URL do Render, senão o Spring
 *    responde 403 "Invalid CORS request".
 * allowCredentials(true) é o que deixa o navegador mandar o cookie de sessão junto.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origens;

    public CorsConfig(@Value("${lesmodromo.cors-origens}") String origens) {
        // tira espaços e a barra do final, que costumam vir junto quando a URL é colada
        this.origens = Arrays.stream(origens.split(","))
                .map(String::trim)
                .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                .filter(o -> !o.isEmpty())
                .toArray(String[]::new);
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
