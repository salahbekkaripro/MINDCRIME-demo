package fr.miage.mindcrime.moteur.contraintes;

import fr.miage.mindcrime.moteur.deduction.*;
import fr.miage.mindcrime.moteur.modele.*;
import fr.miage.mindcrime.moteur.chronologies.Trajets;
import java.util.*;

/** Vérification temporelle limitée au prototype ; les déclarations ne deviennent pas des preuves. */
public final class AnalyseEnquete {
    private final Trajets trajets;
    public AnalyseEnquete(Trajets trajets) { this.trajets = trajets; }
    public ResultatDeduction analyser(Partie partie) {
        Map<String, Partie.Presence> presences = partie.mode() == Partie.Mode.ENQUETEUR
                ? partie.revelations() : partie.declarations();
        return analyser(partie, presences);
    }
    public ResultatDeduction verifierGeneration(Partie partie) {
        return analyser(partie, partie.observations());
    }
    private ResultatDeduction analyser(Partie partie, Map<String, Partie.Presence> presences) {
        var regles = new ArrayList<EvaluateurHypothese>();
        var indices = new HashSet<String>(); indices.add("crime");
        // Ordre stable : même analyse, mêmes explications, indépendamment de Map.copyOf.
        for (var suspect : partie.suspects()) {
            var presence = presences.get(suspect.id());
            if (presence == null) { continue; }
            String reference = "presence-" + suspect.id(); indices.add(reference);
            regles.add((hypothese, contexte) -> {
                var candidat = hypothese.valeur("coupable");
                if (candidat.isEmpty()) { return Evaluation.indeterminee(); }
                if (!candidat.get().equals(suspect.id())) { return Evaluation.compatible(); }
                int trajet = trajets.duree(presence.lieuId(), partie.lieuCrime());
                int disponible = partie.minuteCrime() - presence.minute();
                if (trajet <= disponible) { return Evaluation.compatible(); }
                String nature = partie.mode() == Partie.Mode.ENQUETEUR ? "présence vérifiée" : "déclaration";
                return Evaluation.incompatible(new MotifElimination("deplacement-impossible", List.of("crime", reference),
                        suspect.nom() + " : " + nature + " à " + Carte.lieu(presence.lieuId()).nom()
                                + " à " + Carte.heure(presence.minute()) + ". Trajet minimal : " + trajet
                                + " min, temps disponible : " + disponible + " min."));
            });
        }
        return new MoteurDeduction(regles).analyser(new RequeteDeduction(partie.id(), partie.revision(),
                List.of(new VariableHypothese("coupable", partie.suspects().stream().map(Partie.Suspect::id).toList())),
                "coupable", indices, 1000));
    }
}
