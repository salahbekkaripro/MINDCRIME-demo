package fr.miage.mindcrime.moteur.deduction;

import java.util.List;
import java.util.Set;

/** Exemple manuel avec doublures, sans base, API ou moteurs réels D2/D3. */
public final class DemonstrationDeduction {
    private DemonstrationDeduction() {
    }

    public static void main(String[] args) {
        System.out.println("Démonstration D5 : règles fictives, aucun moteur D2/D3 réel intégré.");
        var variables = List.of(
                new VariableHypothese("coupable", List.of("Alice", "Bruno")),
                new VariableHypothese("lieu", List.of("bureau", "cuisine")));
        EvaluateurHypothese lieuDuCrime = (hypothese, contexte) -> hypothese.valeur("lieu")
                .map(lieu -> lieu.equals("bureau") ? Evaluation.compatible()
                        : Evaluation.incompatible(new MotifElimination("lieu-crime", List.of("P1"),
                                "P1 situe le crime au bureau, pas dans la cuisine.")))
                .orElseGet(Evaluation::indeterminee);
        EvaluateurHypothese alibiAlice = (hypothese, contexte) -> hypothese.valeur("coupable")
                .map(suspect -> suspect.equals("Bruno") ? Evaluation.compatible()
                        : Evaluation.incompatible(new MotifElimination("alibi-Alice", List.of("P2"),
                                "P2 établit qu'Alice ne pouvait pas être présente au moment du crime.")))
                .orElseGet(Evaluation::indeterminee);

        afficher("P1 et P2 accessibles", new MoteurDeduction(List.of(lieuDuCrime, alibiAlice))
                .analyser(new RequeteDeduction("partie-demo", 1, variables, "coupable", Set.of("P1", "P2"), 100)));
        afficher("P2 retiré : deux coupables restent possibles", new MoteurDeduction(List.of(lieuDuCrime))
                .analyser(new RequeteDeduction("partie-demo", 2, variables, "coupable", Set.of("P1"), 100)));
        afficher("Budget limité à la racine", new MoteurDeduction(List.of(lieuDuCrime, alibiAlice))
                .analyser(new RequeteDeduction("partie-demo", 3, variables, "coupable", Set.of("P1", "P2"), 1)));
    }

    private static void afficher(String titre, ResultatDeduction resultat) {
        System.out.println("\n" + titre);
        System.out.println("Conclusion : " + resultat.conclusion());
        System.out.println("Analyse complète : " + resultat.analyseComplete());
        System.out.println("Scénarios : " + resultat.scenariosCompatibles());
        System.out.println("Coupables identifiés : " + resultat.coupablesIdentifies());
        System.out.println("Nœuds visités : " + resultat.noeudsVisites());
        for (Elimination elimination : resultat.eliminations()) {
            System.out.println("Élimination : " + elimination.hypothese().affectations());
            elimination.motifs().forEach(motif -> System.out.println("  " + motif.description()));
        }
    }
}
