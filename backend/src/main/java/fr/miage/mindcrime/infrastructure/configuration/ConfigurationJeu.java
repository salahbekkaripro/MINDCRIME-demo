package fr.miage.mindcrime.infrastructure.configuration;
import fr.miage.mindcrime.application.*;
import fr.miage.mindcrime.application.port.StockageParties;
import fr.miage.mindcrime.moteur.chronologies.Trajets;
import fr.miage.mindcrime.moteur.contraintes.AnalyseEnquete;
import fr.miage.mindcrime.moteur.generation.GenerateurEnquetes;
import org.springframework.context.annotation.*;
import java.security.SecureRandom;

@Configuration
public class ConfigurationJeu {
    @Bean Trajets trajets() { return new Trajets(); }
    @Bean AnalyseEnquete analyseEnquete(Trajets trajets) { return new AnalyseEnquete(trajets); }
    @Bean GenerateurEnquetes generateur(Trajets trajets, AnalyseEnquete analyse) {
        return new GenerateurEnquetes(new SecureRandom(), trajets, analyse);
    }
    @Bean ServiceParties serviceParties(StockageParties stockage, GenerateurEnquetes generateur, AnalyseEnquete analyse) {
        return new ServiceParties(stockage, generateur, analyse);
    }
}
