package fr.miage.mindcrime.moteur.deduction;

import java.util.Objects;

/** Vérifications communes aux valeurs du contrat D5. */
final class Verification {
    private Verification() {
    }

    static String texte(String valeur, String champ) {
        Objects.requireNonNull(valeur, champ);
        if (valeur.isBlank()) {
            throw new IllegalArgumentException(champ + " ne doit pas être vide");
        }
        return valeur;
    }
}
