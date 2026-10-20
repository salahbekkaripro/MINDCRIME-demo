package fr.miage.mindcrime.moteur.deduction;

import java.util.List;
import java.util.Objects;

/** Branche incompatible, qui peut être partielle, avec ses motifs. */
public record Elimination(Hypothese hypothese, List<MotifElimination> motifs) {
    public Elimination {
        Objects.requireNonNull(hypothese, "hypothese");
        motifs = List.copyOf(motifs);
        if (motifs.isEmpty()) {
            throw new IllegalArgumentException("Élimination sans motif");
        }
    }
}
