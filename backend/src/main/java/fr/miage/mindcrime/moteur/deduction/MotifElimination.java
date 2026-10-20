package fr.miage.mindcrime.moteur.deduction;

import java.util.HashSet;
import java.util.List;

/** Explication structurée dont les références doivent appartenir au contexte autorisé. */
public record MotifElimination(String regleId, List<String> indices, String description) {
    public MotifElimination {
        regleId = Verification.texte(regleId, "regleId");
        description = Verification.texte(description, "description");
        indices = List.copyOf(indices);
        indices.forEach(indice -> Verification.texte(indice, "indice"));
        if (indices.isEmpty() || new HashSet<>(indices).size() != indices.size()) {
            throw new IllegalArgumentException("Un motif requiert des indices distincts et non vides");
        }
    }
}
