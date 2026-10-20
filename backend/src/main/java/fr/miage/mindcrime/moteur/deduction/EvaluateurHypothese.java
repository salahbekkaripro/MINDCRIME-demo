package fr.miage.mindcrime.moteur.deduction;

/**
 * Contrat de vérification à implémenter par les domaines concernés.
 * Une incompatibilité partielle doit interdire toutes les extensions de la branche.
 * L'évaluateur doit être déterministe et n'utiliser que les informations autorisées.
 */
@FunctionalInterface
public interface EvaluateurHypothese {
    Evaluation evaluer(Hypothese hypothese, RequeteDeduction contexte);
}
