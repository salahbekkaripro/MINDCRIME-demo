package fr.miage.mindcrime.moteur.deduction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Données d'analyse déjà autorisées par le cas d'utilisation, jamais par le client seul. */
public record RequeteDeduction(
        String partieId,
        long revision,
        List<VariableHypothese> variables,
        String variableCoupable,
        Set<String> indicesAccessibles,
        long maxNoeuds) {

    public RequeteDeduction {
        partieId = Verification.texte(partieId, "partieId");
        variableCoupable = Verification.texte(variableCoupable, "variableCoupable");
        variables = List.copyOf(variables);
        indicesAccessibles = Set.copyOf(indicesAccessibles);
        indicesAccessibles.forEach(indice -> Verification.texte(indice, "indice"));
        if (revision < 0 || maxNoeuds <= 0) {
            throw new IllegalArgumentException("Révision négative ou budget non positif");
        }
        Set<String> noms = new HashSet<>();
        for (VariableHypothese variable : variables) {
            if (!noms.add(variable.id())) {
                throw new IllegalArgumentException("Variable dupliquée : " + variable.id());
            }
        }
        if (!noms.contains(variableCoupable)) {
            throw new IllegalArgumentException("Variable coupable absente : " + variableCoupable);
        }
    }
}
