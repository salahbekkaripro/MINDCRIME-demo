package fr.miage.mindcrime.moteur.deduction;

import java.util.List;
import java.util.Objects;

public record Evaluation(Statut statut, List<MotifElimination> motifs) {
    public enum Statut {
        COMPATIBLE, INCOMPATIBLE, INDETERMINE
    }

    public Evaluation {
        Objects.requireNonNull(statut, "statut");
        motifs = List.copyOf(motifs);
        if ((statut == Statut.INCOMPATIBLE) != !motifs.isEmpty()) {
            throw new IllegalArgumentException("Seule une incompatibilité possède des motifs, obligatoires");
        }
    }

    public static Evaluation compatible() {
        return new Evaluation(Statut.COMPATIBLE, List.of());
    }

    public static Evaluation indeterminee() {
        return new Evaluation(Statut.INDETERMINE, List.of());
    }

    public static Evaluation incompatible(MotifElimination motif) {
        return new Evaluation(Statut.INCOMPATIBLE, List.of(motif));
    }
}
