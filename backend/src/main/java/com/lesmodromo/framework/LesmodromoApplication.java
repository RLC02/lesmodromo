package com.lesmodromo.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada. Fica dentro de "framework" de propósito: o Spring só enxerga essa camada.
 * O core não tem nenhuma anotação; os casos de uso viram beans em config/UseCaseConfig.
 */
@SpringBootApplication
public class LesmodromoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LesmodromoApplication.class, args);
    }
}
