package fr.miage.mindcrime.moteur.deduction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Instantané immuable d'une affectation, complète ou partielle. */
public record Hypothese(Map<String, String> affectations) {
    public Hypothese {
        Map<String, String> copie = new LinkedHashMap<>();
        affectations.forEach((variable, valeur) -> copie.put(
                Verification.texte(variable, "variable"), Verification.texte(valeur, "valeur")));
        affectations = Collections.unmodifiableMap(copie);
    }

    public Optional<String> valeur(String variable) {
        return Optional.ofNullable(affectations.get(variable));
    }
}
