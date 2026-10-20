package fr.miage.mindcrime.moteur.modele;

import java.util.*;

/** Données internes : jamais sérialisées directement par un contrôleur. */
public record Partie(String id, Mode mode, Etat etat, long revision, String lieuCrime,
                     int minuteCrime, String coupableId, List<Suspect> suspects,
                     Map<String, Presence> observations, Map<String, Presence> revelations,
                     Map<String, Presence> declarations, String message) {
    public enum Mode { ENQUETEUR, ALIBI }
    public enum Etat { EN_COURS, RESOLUE, TERMINEE }
    public record Suspect(String id, String nom, String role) { }
    public record Presence(String lieuId, int minute) {
        public Presence {
            Carte.lieu(lieuId);
            if (minute < 1140 || minute > 1200) {
                throw new IllegalArgumentException("L’horaire doit être compris entre 19 h et 20 h");
            }
        }
    }
    public Partie {
        suspects = List.copyOf(suspects); observations = Map.copyOf(observations);
        revelations = Map.copyOf(revelations); declarations = Map.copyOf(declarations);
    }
    public Suspect suspect(String suspectId) {
        return suspects.stream().filter(s -> s.id().equals(suspectId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Suspect inconnu"));
    }
    public void verifierEnCours() {
        if (etat != Etat.EN_COURS) { throw new PartieFermee("Cette partie est terminée"); }
    }
    public Partie interroger(String suspectId) {
        verifierEnCours(); suspect(suspectId);
        if (mode != Mode.ENQUETEUR) { throw new PartieFermee("Saisis un témoignage dans le mode Alibi"); }
        var connus = new LinkedHashMap<>(revelations); connus.put(suspectId, observations.get(suspectId));
        return copie(etat, connus, declarations, "Le témoignage est recoupé avec une présence vérifiée.");
    }
    public Partie temoigner(String suspectId, Presence presence) {
        verifierEnCours(); suspect(suspectId);
        if (mode != Mode.ALIBI) { throw new PartieFermee("Les témoignages sont réservés au mode Alibi"); }
        var temoignages = new LinkedHashMap<>(declarations); temoignages.put(suspectId, presence);
        return copie(etat, revelations, temoignages, "Déclaration enregistrée. Elle peut être vraie ou mensongère.");
    }
    public Partie terminer(Etat nouvelEtat, String texte) {
        verifierEnCours(); return copie(nouvelEtat, revelations, declarations, texte);
    }
    public Partie message(String texte) {
        verifierEnCours(); return copie(etat, revelations, declarations, texte);
    }
    private Partie copie(Etat nouvelEtat, Map<String, Presence> connus,
                         Map<String, Presence> temoignages, String texte) {
        return new Partie(id, mode, nouvelEtat, revision + 1, lieuCrime, minuteCrime,
                coupableId, suspects, observations, connus, temoignages, texte);
    }
    public static final class PartieFermee extends RuntimeException {
        public PartieFermee(String message) { super(message); }
    }
}
