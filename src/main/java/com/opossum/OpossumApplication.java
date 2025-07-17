package com.opossum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale de l'application OPOSSUM Backend. Application Spring Boot
 * pour la gestion des objets perdus / trouvés.
 */
@SpringBootApplication
public class OpossumApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpossumApplication.class, args);
    }
}
