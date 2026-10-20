package fr.miage.mindcrime.api.rest;
import fr.miage.mindcrime.moteur.deduction.ResultatDeduction;
import fr.miage.mindcrime.moteur.modele.*;
import java.util.*;

/** DTO explicite : observations cachées et coupable réel exclus, y compris des listes. */
public record VuePartie(String id, Partie.Mode mode, Partie.Etat etat, long revision,
                        String lieuCrime, int minuteCrime, String message,
                        List<VueSuspect> suspects, List<Carte.Lieu> lieux, List<Carte.Liaison> liaisons,
                        VueAnalyse analyse) {
    public record VueSuspect(String id, String nom, String role, Partie.Presence presence) { }
    public record VueAnalyse(String conclusion, boolean complete, List<String> possibles,
                              List<String> explications, long noeuds, boolean conditionnelle) { }
    public static VuePartie depuis(Partie partie, ResultatDeduction resultat) {
        var presences = partie.mode() == Partie.Mode.ENQUETEUR ? partie.revelations() : partie.declarations();
        var suspects = partie.suspects().stream().map(s -> new VueSuspect(s.id(), s.nom(), s.role(), presences.get(s.id()))).toList();
        var explications = resultat.eliminations().stream().flatMap(e -> e.motifs().stream())
                .map(m -> m.description()).distinct().toList();
        var analyse = new VueAnalyse(resultat.conclusion().name(), resultat.analyseComplete(),
                List.copyOf(resultat.coupablesIdentifies()), explications, resultat.noeudsVisites(), partie.mode() == Partie.Mode.ALIBI);
        return new VuePartie(partie.id(), partie.mode(), partie.etat(), partie.revision(), partie.lieuCrime(),
                partie.minuteCrime(), partie.message(), suspects, Carte.LIEUX, Carte.LIAISONS, analyse);
    }
}
