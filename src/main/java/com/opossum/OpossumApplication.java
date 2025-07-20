
// Classe principale de l'application Spring Boot Opossum
package com.opossum;


// Importation de Dotenv pour charger les variables d'environnement depuis le fichier .env
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Point d'entrée principal de l'application Opossum (Spring Boot).
 * <p>
 * - Charge les variables d'environnement depuis le fichier .env grâce à Dotenv
 * - Démarre le contexte Spring Boot
 */
@SpringBootApplication(scanBasePackages = "com.opossum")
public class OpossumApplication {

    /**
     * Méthode main : exécution de l'application.
     * - Charge le fichier .env si présent (ignore si absent)
     * - Ajoute chaque variable d'environnement dans les propriétés système
     * - Lance l'application Spring Boot
     */
    public static void main(String[] args) {
        // Chargement des variables d'environnement depuis .env
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        // Démarrage de l'application Spring Boot
        SpringApplication.run(OpossumApplication.class, args);
    }
}
