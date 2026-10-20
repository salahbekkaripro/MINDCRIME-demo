package fr.miage.mindcrime.moteur.deduction;

import java.util.HashSet;
import java.util.List;

/** Variable à domaine fini, sans valeurs vides ou dupliquées. */
public record VariableHypothese(String id, List<String> valeurs) {
    public VariableHypothese {
        id = Verification.texte(id, "id");
        valeurs = List.copyOf(valeurs);
        if (valeurs.isEmpty()) {
            throw new IllegalArgumentException("Le domaine de " + id + " est vide");
        }
        valeurs.forEach(valeur -> Verification.texte(valeur, "valeur"));
        if (new HashSet<>(valeurs).size() != valeurs.size()) {
            throw new IllegalArgumentException("Valeurs dupliquées pour " + id);
        }
    }
}
