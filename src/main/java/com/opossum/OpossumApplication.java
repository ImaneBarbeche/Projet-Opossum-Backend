
// Package principal de l'application
package com.opossum;


// Importation de la librairie dotenv pour charger les variables d'environnement depuis un fichier .env
import io.github.cdimascio.dotenv.Dotenv;
// Importations Spring Boot pour le démarrage de l'application
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Annotation principale qui active la configuration automatique de Spring Boot
// et indique à Spring de scanner le package com.opossum et ses sous-packages
@SpringBootApplication(scanBasePackages = "com.opossum")

// Classe principale qui sert de point d'entrée à l'application Spring Boot
public class OpossumApplication {

    public static void main(String[] args) {
        // Chargement des variables d'environnement depuis le fichier .env (si présent)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // Ajout de chaque variable d'environnement aux propriétés système
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        // Démarrage de l'application Spring Boot
        SpringApplication.run(OpossumApplication.class, args);
    }
}