package fr.miage.mindcrime.moteur.deduction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Recherche exhaustive en profondeur avec élagage certain et budget de nœuds. */
public final class MoteurDeduction {
    private final List<EvaluateurHypothese> evaluateurs;

    public MoteurDeduction(List<EvaluateurHypothese> evaluateurs) {
        this.evaluateurs = List.copyOf(evaluateurs);
    }

    public ResultatDeduction analyser(RequeteDeduction requete) {
        Objects.requireNonNull(requete, "requete");
        List<Hypothese> compatibles = new ArrayList<>();
        List<Hypothese> nonResolus = new ArrayList<>();
        List<Elimination> eliminations = new ArrayList<>();
        Map<String, String> affectations = new LinkedHashMap<>();
        Deque<Etape> pile = new ArrayDeque<>();
        pile.push(new Etape(0));
        long visites = 0;
        boolean interrompue = false;

        while (!pile.isEmpty()) {
            Etape etape = pile.peek();
            if (!etape.evaluee) {
                if (visites == requete.maxNoeuds()) {
                    interrompue = true;
                    break;
                }
                visites++;
                Hypothese instantane = new Hypothese(affectations);
                Evaluation evaluation = evaluer(instantane, requete);
                if (evaluation.statut() == Evaluation.Statut.INCOMPATIBLE) {
                    eliminations.add(new Elimination(instantane, evaluation.motifs()));
                    remonter(pile, affectations, requete);
                    continue;
                }
                if (etape.profondeur == requete.variables().size()) {
                    if (evaluation.statut() == Evaluation.Statut.COMPATIBLE) {
                        compatibles.add(instantane);
                    } else {
                        nonResolus.add(instantane);
                    }
                    remonter(pile, affectations, requete);
                    continue;
                }
                etape.evaluee = true;
            }

            VariableHypothese variable = requete.variables().get(etape.profondeur);
            if (etape.prochaineValeur == variable.valeurs().size()) {
                remonter(pile, affectations, requete);
            } else {
                affectations.put(variable.id(), variable.valeurs().get(etape.prochaineValeur++));
                pile.push(new Etape(etape.profondeur + 1));
            }
        }
        return new ResultatDeduction(requete, compatibles, eliminations, nonResolus, visites, !interrompue);
    }

    private Evaluation evaluer(Hypothese hypothese, RequeteDeduction requete) {
        List<MotifElimination> motifs = new ArrayList<>();
        boolean indeterminee = false;
        for (EvaluateurHypothese evaluateur : evaluateurs) {
            Evaluation evaluation = Objects.requireNonNull(
                    evaluateur.evaluer(hypothese, requete), "Évaluation absente");
            for (MotifElimination motif : evaluation.motifs()) {
                if (!requete.indicesAccessibles().containsAll(motif.indices())) {
                    throw new IllegalStateException("Motif utilisant un indice inconnu ou inaccessible");
                }
                motifs.add(motif);
            }
            indeterminee |= evaluation.statut() == Evaluation.Statut.INDETERMINE;
        }
        if (!motifs.isEmpty()) {
            return new Evaluation(Evaluation.Statut.INCOMPATIBLE, motifs);
        }
        return indeterminee ? Evaluation.indeterminee() : Evaluation.compatible();
    }

    private static void remonter(Deque<Etape> pile, Map<String, String> affectations,
            RequeteDeduction requete) {
        Etape terminee = pile.pop();
        if (terminee.profondeur > 0) {
            affectations.remove(requete.variables().get(terminee.profondeur - 1).id());
        }
    }

    /** Une étape ne conserve pas de copie du chemin : la pile reste linéaire en profondeur. */
    private static final class Etape {
        private final int profondeur;
        private int prochaineValeur;
        private boolean evaluee;

        private Etape(int profondeur) {
            this.profondeur = profondeur;
        }
    }
}
