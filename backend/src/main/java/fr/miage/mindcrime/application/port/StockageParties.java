package fr.miage.mindcrime.application.port;

import fr.miage.mindcrime.moteur.modele.Partie;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface StockageParties {
    Partie creer(Partie partie);
    Optional<Partie> lire(String id);
    Partie modifier(String id, UnaryOperator<Partie> modification);
    List<Partie> recentes();
}
