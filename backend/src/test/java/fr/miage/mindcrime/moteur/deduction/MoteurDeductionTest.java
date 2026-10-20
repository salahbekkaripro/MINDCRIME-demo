package fr.miage.mindcrime.moteur.deduction;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static fr.miage.mindcrime.moteur.deduction.ResultatDeduction.Conclusion.*;
import static org.junit.jupiter.api.Assertions.*;

/** Doublures de règles ; aucun moteur réel D2/D3 n'est simulé comme déjà intégré. */
class MoteurDeductionTest {
    private static final List<VariableHypothese> VARIABLES = List.of(
            new VariableHypothese("coupable", List.of("Alice", "Bruno")),
            new VariableHypothese("lieu", List.of("bureau", "cuisine")));

    private static RequeteDeduction requete(List<VariableHypothese> variables, long budget) {
        return new RequeteDeduction("partie-1", 7, variables, "coupable",
                Set.of("P1", "P2", "P3"), budget);
    }

    private static EvaluateurHypothese regle(String variable, String valeur, boolean egalite, String indice) {
        return (hypothese, contexte) -> hypothese.valeur(variable)
                .map(actual -> actual.equals(valeur) == egalite
                        ? Evaluation.compatible()
                        : Evaluation.incompatible(new MotifElimination(
                                "regle-" + indice, List.of(indice), "Incompatibilité de " + variable)))
                .orElseGet(Evaluation::indeterminee);
    }

    private static EvaluateurHypothese lieuBureau() {
        return regle("lieu", "bureau", true, "P1");
    }

    private static EvaluateurHypothese exclureAlice() {
        return regle("coupable", "Alice", false, "P2");
    }

    @Test
    void conserveLaSolutionEtLesMotifsDesBranchesEliminees() {
        var resultat = new MoteurDeduction(List.of(lieuBureau(), exclureAlice()))
                .analyser(requete(VARIABLES, 100));
        assertEquals(List.of(new Hypothese(Map.of("coupable", "Bruno", "lieu", "bureau"))),
                resultat.scenariosCompatibles());
        assertEquals(SCENARIO_UNIQUE, resultat.conclusion());
        assertTrue(resultat.coupableUnique());
        assertEquals(Set.of("Bruno"), resultat.coupablesIdentifies());
        assertEquals(5, resultat.noeudsVisites());
        assertEquals(2, resultat.eliminations().size());
        assertEquals(Map.of("coupable", "Alice"), resultat.eliminations().getFirst().hypothese().affectations());
        assertEquals(List.of("P2"), resultat.eliminations().getFirst().motifs().getFirst().indices());
        assertEquals(List.of("P1"), resultat.eliminations().getLast().motifs().getFirst().indices());
        assertEquals(7, resultat.contexte().revision());
        assertEquals("partie-1", resultat.contexte().partieId());
    }

    @Test
    void retirerUnIndiceConserveLesDeuxSuspects() {
        var contexte = new RequeteDeduction("partie-1", 8, VARIABLES, "coupable", Set.of("P1"), 100);
        var resultat = new MoteurDeduction(List.of(lieuBureau())).analyser(contexte);
        assertEquals(PLUSIEURS_SCENARIOS, resultat.conclusion());
        assertEquals(2, resultat.scenariosCompatibles().size());
        assertEquals(Set.of("Alice", "Bruno"), resultat.coupablesIdentifies());
        assertFalse(resultat.coupableUnique());
    }

    @Test
    void plusieursScenariosPeuventAvoirLeMemeCoupable() {
        var variables = new ArrayList<>(VARIABLES);
        variables.add(new VariableHypothese("creneau", List.of("T1", "T2")));
        var resultat = new MoteurDeduction(List.of(lieuBureau(), exclureAlice()))
                .analyser(requete(variables, 100));
        assertEquals(PLUSIEURS_SCENARIOS, resultat.conclusion());
        assertEquals(2, resultat.scenariosCompatibles().size());
        assertTrue(resultat.coupableUnique());
    }

    @Test
    void contradictionsCompletesNeDesignentAucunCoupable() {
        var resultat = new MoteurDeduction(List.of(lieuBureau(), exclureAlice(),
                regle("coupable", "Bruno", false, "P3"))).analyser(requete(VARIABLES, 100));
        assertEquals(AUCUN_SCENARIO, resultat.conclusion());
        assertTrue(resultat.analyseComplete());
        assertTrue(resultat.coupablesIdentifies().isEmpty());
        assertFalse(resultat.coupableUnique());
    }

    @Test
    void uneEvaluationIndetermineeNeSupprimePasUneBranche() {
        EvaluateurHypothese douteAlice = (h, c) -> h.valeur("coupable")
                .filter("Bruno"::equals).map(ignored -> Evaluation.compatible())
                .orElseGet(Evaluation::indeterminee);
        var resultat = new MoteurDeduction(List.of(lieuBureau(), douteAlice))
                .analyser(requete(VARIABLES, 100));
        assertTrue(resultat.rechercheExhaustive());
        assertFalse(resultat.analyseComplete());
        assertFalse(resultat.budgetInterrompu());
        assertEquals(INDETERMINE, resultat.conclusion());
        assertEquals(List.of(new Hypothese(Map.of("coupable", "Alice", "lieu", "bureau"))),
                resultat.scenariosNonResolus());
        assertEquals(1, resultat.scenariosCompatibles().size());
        assertFalse(resultat.coupableUnique());
    }

    @Test
    void budgetRacineNeSignifiePasAucuneSolution() {
        var resultat = new MoteurDeduction(List.of(lieuBureau(), exclureAlice()))
                .analyser(requete(VARIABLES, 1));
        assertEquals(1, resultat.noeudsVisites());
        assertTrue(resultat.budgetInterrompu());
        assertEquals(INDETERMINE, resultat.conclusion());
        assertTrue(resultat.scenariosCompatibles().isEmpty());
    }

    @Test
    void unScenarioTrouveAvantInterruptionNeProuvePasLUnicite() {
        var resultat = new MoteurDeduction(List.of()).analyser(requete(VARIABLES, 3));
        assertEquals(1, resultat.scenariosCompatibles().size());
        assertEquals(INDETERMINE, resultat.conclusion());
        assertFalse(resultat.coupableUnique());
        assertTrue(resultat.budgetInterrompu());
    }

    @Test
    void terminerExactementAuBudgetResteExhaustif() {
        var resultat = new MoteurDeduction(List.of(lieuBureau(), exclureAlice()))
                .analyser(requete(VARIABLES, 5));
        assertEquals(5, resultat.noeudsVisites());
        assertTrue(resultat.analyseComplete());
        assertFalse(resultat.budgetInterrompu());
        assertEquals(SCENARIO_UNIQUE, resultat.conclusion());
    }

    @Test
    void uneContradictionCertainePrimeSurUneEvaluationIndeterminee() {
        var resultat = new MoteurDeduction(List.of((h, c) -> Evaluation.indeterminee(),
                (h, c) -> Evaluation.incompatible(new MotifElimination("impossible", List.of("P1"), "Fait incompatible"))))
                .analyser(requete(VARIABLES, 1));
        assertEquals(AUCUN_SCENARIO, resultat.conclusion());
        assertEquals(1, resultat.noeudsVisites());
        assertTrue(resultat.eliminations().getFirst().hypothese().affectations().isEmpty());
    }

    @Test
    void refuseUnIndiceInconnuOuCacheAuLieuDeMasquerLeMotif() {
        var moteur = new MoteurDeduction(List.of((h, c) -> Evaluation.incompatible(
                new MotifElimination("cache", List.of("SECRET"), "Ne doit pas être exposé"))));
        assertThrows(IllegalStateException.class, () -> moteur.analyser(requete(VARIABLES, 100)));
    }

    @Test
    void transmetUneErreurDeVerificationSansLaTransformerEnContradiction() {
        var panne = new IllegalStateException("Évaluateur indisponible");
        var moteur = new MoteurDeduction(List.of((h, c) -> { throw panne; }));
        assertSame(panne, assertThrows(IllegalStateException.class,
                () -> moteur.analyser(requete(VARIABLES, 100))));
    }

    @Test
    void refuseUneEvaluationNulle() {
        var moteur = new MoteurDeduction(List.of((h, c) -> null));
        assertThrows(NullPointerException.class, () -> moteur.analyser(requete(VARIABLES, 100)));
    }

    @Test
    void nePartagePasLesAffectationsModifiablesEntreEvaluateursEtBranches() {
        var observes = new ArrayList<Hypothese>();
        EvaluateurHypothese observateur = (h, c) -> {
            observes.add(h);
            assertThrows(UnsupportedOperationException.class, () -> h.affectations().put("intrus", "x"));
            return Evaluation.compatible();
        };
        var resultat = new MoteurDeduction(List.of(observateur)).analyser(requete(VARIABLES, 100));
        assertTrue(observes.getFirst().affectations().isEmpty());
        assertEquals(4, new HashSet<>(resultat.scenariosCompatibles()).size());
        assertEquals(Set.of("Alice", "Bruno"), resultat.coupablesIdentifies());
        assertThrows(UnsupportedOperationException.class, () -> resultat.scenariosCompatibles().clear());
        assertThrows(UnsupportedOperationException.class, () -> resultat.coupablesIdentifies().clear());
    }

    @Test
    void lesRequetesEtHypothesesCopientLeursCollectionsDentree() {
        var valeurs = new ArrayList<>(List.of("Alice", "Bruno"));
        var variables = new ArrayList<>(List.of(new VariableHypothese("coupable", valeurs)));
        var indices = new HashSet<>(Set.of("P1"));
        var requete = new RequeteDeduction("p", 0, variables, "coupable", indices, 20);
        var affectations = new LinkedHashMap<>(Map.of("coupable", "Alice"));
        var hypothese = new Hypothese(affectations);
        valeurs.clear(); variables.clear(); indices.clear(); affectations.clear();
        assertEquals(List.of("Alice", "Bruno"), requete.variables().getFirst().valeurs());
        assertEquals(Set.of("P1"), requete.indicesAccessibles());
        assertEquals(Map.of("coupable", "Alice"), hypothese.affectations());
    }

    @Test
    void refuseLesDomainesVidesEtDupliquesEtLesVariablesIncoherentes() {
        assertThrows(IllegalArgumentException.class, () -> new VariableHypothese("coupable", List.of()));
        assertThrows(IllegalArgumentException.class, () -> new VariableHypothese("coupable", List.of("A", "A")));
        assertThrows(IllegalArgumentException.class, () -> new VariableHypothese(" ", List.of("A")));
        assertThrows(IllegalArgumentException.class, () -> new VariableHypothese("x", List.of(" ")));
        assertThrows(IllegalArgumentException.class, () -> requete(List.of(VARIABLES.getFirst(), VARIABLES.getFirst()), 10));
        assertThrows(IllegalArgumentException.class, () -> requete(List.of(), 10));
        assertThrows(IllegalArgumentException.class, () -> requete(VARIABLES, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new RequeteDeduction("p", -1, VARIABLES, "coupable", Set.of(), 10));
    }

    @Test
    void imposeDesMotifsCohérentsAvecLeStatut() {
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluation(Evaluation.Statut.INCOMPATIBLE, List.of()));
        var motif = new MotifElimination("r", List.of("P1"), "explication");
        assertThrows(IllegalArgumentException.class,
                () -> new Evaluation(Evaluation.Statut.COMPATIBLE, List.of(motif)));
        assertThrows(IllegalArgumentException.class, () -> new MotifElimination("r", List.of(), "explication"));
        assertThrows(IllegalArgumentException.class, () -> new MotifElimination("r", List.of("P1", "P1"), "explication"));
    }

    @Test
    void eliminerUneBrancheEviteDeVisiterToutesSesExtensions() {
        var variables = List.of(VARIABLES.getFirst(),
                new VariableHypothese("lieu", List.of("L1", "L2", "L3", "L4")),
                new VariableHypothese("creneau", List.of("T1", "T2", "T3", "T4")));
        var appels = new AtomicLong();
        EvaluateurHypothese observateur = (h, c) -> { appels.incrementAndGet(); return Evaluation.compatible(); };
        var sansElagage = new MoteurDeduction(List.of(observateur)).analyser(requete(variables, 100));
        var avecElagage = new MoteurDeduction(List.of(exclureAlice(), observateur))
                .analyser(requete(variables, 100));
        assertEquals(32, sansElagage.scenariosCompatibles().size());
        assertEquals(16, avecElagage.scenariosCompatibles().size());
        assertTrue(avecElagage.noeudsVisites() < sansElagage.noeudsVisites());
    }

    @Test
    void uneLongueSuiteDeVariablesNeDependPasDeLaPileRecursiveJava() {
        var variables = new ArrayList<VariableHypothese>();
        variables.add(new VariableHypothese("coupable", List.of("Alice")));
        for (int i = 0; i < 1500; i++) {
            variables.add(new VariableHypothese("v" + i, List.of("x")));
        }
        var resultat = new MoteurDeduction(List.of()).analyser(requete(variables, 2000));
        assertEquals(SCENARIO_UNIQUE, resultat.conclusion());
        assertEquals(1502, resultat.noeudsVisites());
        assertEquals(1501, resultat.scenariosCompatibles().getFirst().affectations().size());
    }

    @Test
    void egaleUneEnumerationExhaustiveIndependanteSurDesContraintesAleatoires() {
        var aleatoire = new Random(20261020L);
        var variables = List.of(new VariableHypothese("coupable", List.of("0", "1", "2")),
                new VariableHypothese("lieu", List.of("0", "1", "2")),
                new VariableHypothese("creneau", List.of("0", "1", "2")));
        for (int essai = 0; essai < 60; essai++) {
            Set<String> interdits = new HashSet<>();
            for (int a = 0; a < 3; a++) {
                for (int b = 0; b < 3; b++) {
                    if (aleatoire.nextBoolean()) { interdits.add(a + ":" + b); }
                }
            }
            EvaluateurHypothese contrainte = (h, c) -> {
                var suspect = h.valeur("coupable");
                var lieu = h.valeur("lieu");
                if (suspect.isEmpty() || lieu.isEmpty()) { return Evaluation.indeterminee(); }
                return interdits.contains(suspect.get() + ":" + lieu.get())
                        ? Evaluation.incompatible(new MotifElimination("paire", List.of("P1"), "Paire interdite"))
                        : Evaluation.compatible();
            };
            Set<Hypothese> attendu = new HashSet<>();
            for (int a = 0; a < 3; a++) {
                for (int b = 0; b < 3; b++) {
                    for (int c = 0; c < 3; c++) {
                        if (!interdits.contains(a + ":" + b)) {
                            attendu.add(new Hypothese(Map.of("coupable", "" + a, "lieu", "" + b, "creneau", "" + c)));
                        }
                    }
                }
            }
            var moteur = new MoteurDeduction(List.of(contrainte));
            var resultat = moteur.analyser(requete(variables, 1000));
            assertEquals(attendu, new HashSet<>(resultat.scenariosCompatibles()), "essai " + essai);
            var inversees = new ArrayList<>(variables);
            Collections.reverse(inversees);
            assertEquals(attendu, new HashSet<>(moteur.analyser(requete(inversees, 1000)).scenariosCompatibles()));
            var valeursInversees = variables.stream().map(variable -> {
                var valeurs = new ArrayList<>(variable.valeurs());
                Collections.reverse(valeurs);
                return new VariableHypothese(variable.id(), valeurs);
            }).toList();
            assertEquals(attendu, new HashSet<>(moteur.analyser(requete(valeursInversees, 1000)).scenariosCompatibles()));
            assertEquals(resultat.scenariosCompatibles(), moteur.analyser(requete(variables, 1000)).scenariosCompatibles());
            assertEquals(attendu.size(), resultat.scenariosCompatibles().size());
        }
    }
}
