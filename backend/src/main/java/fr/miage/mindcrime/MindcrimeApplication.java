package fr.miage.mindcrime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Point d'entrée technique ; aucun algorithme métier n'est implémenté ici. */
@SpringBootApplication
public class MindcrimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindcrimeApplication.class, args);
    }
}
