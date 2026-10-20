package fr.miage.mindcrime.moteur.deduction;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Résultats vérifiés et incertitudes, toujours liés à la révision analysée. */
public final class ResultatDeduction {
    public enum Conclusion {
        AUCUN_SCENARIO, SCENARIO_UNIQUE, PLUSIEURS_SCENARIOS, INDETERMINE
    }

    private final RequeteDeduction contexte;
    private final List<Hypothese> scenariosCompatibles;
    private final List<Elimination> eliminations;
    private final List<Hypothese> scenariosNonResolus;
    private final long noeudsVisites;
    private final boolean rechercheExhaustive;

    ResultatDeduction(RequeteDeduction contexte, List<Hypothese> scenariosCompatibles,
            List<Elimination> eliminations, List<Hypothese> scenariosNonResolus,
            long noeudsVisites, boolean rechercheExhaustive) {
        this.contexte = contexte;
        this.scenariosCompatibles = List.copyOf(scenariosCompatibles);
        this.eliminations = List.copyOf(eliminations);
        this.scenariosNonResolus = List.copyOf(scenariosNonResolus);
        this.noeudsVisites = noeudsVisites;
        this.rechercheExhaustive = rechercheExhaustive;
    }

    public RequeteDeduction contexte() {
        return contexte;
    }

    public List<Hypothese> scenariosCompatibles() {
        return scenariosCompatibles;
    }

    public List<Elimination> eliminations() {
        return eliminations;
    }

    public List<Hypothese> scenariosNonResolus() {
        return scenariosNonResolus;
    }

    public long noeudsVisites() {
        return noeudsVisites;
    }

    public boolean rechercheExhaustive() {
        return rechercheExhaustive;
    }

    public boolean budgetInterrompu() {
        return !rechercheExhaustive;
    }

    public boolean analyseComplete() {
        return rechercheExhaustive && scenariosNonResolus.isEmpty();
    }

    public Conclusion conclusion() {
        if (!analyseComplete()) {
            return Conclusion.INDETERMINE;
        }
        return switch (scenariosCompatibles.size()) {
            case 0 -> Conclusion.AUCUN_SCENARIO;
            case 1 -> Conclusion.SCENARIO_UNIQUE;
            default -> Conclusion.PLUSIEURS_SCENARIOS;
        };
    }

    /** Ensemble exhaustif seulement si analyseComplete() est vrai. */
    public Set<String> coupablesIdentifies() {
        Set<String> coupables = new LinkedHashSet<>();
        for (Hypothese hypothese : scenariosCompatibles) {
            coupables.add(hypothese.affectations().get(contexte.variableCoupable()));
        }
        return Collections.unmodifiableSet(coupables);
    }

    public boolean coupableUnique() {
        return analyseComplete() && coupablesIdentifies().size() == 1;
    }
}
