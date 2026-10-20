package fr.miage.mindcrime.moteur.generation;

import fr.miage.mindcrime.moteur.chronologies.Trajets;
import fr.miage.mindcrime.moteur.contraintes.AnalyseEnquete;
import fr.miage.mindcrime.moteur.modele.*;
import java.util.*;
import java.util.random.RandomGenerator;

/** Génération paramétrique bornée, contrôlée par le moteur commun ; pas un générateur narratif général. */
public final class GenerateurEnquetes {
    private final RandomGenerator hasard;
    private final Trajets trajets;
    private final AnalyseEnquete analyse;
    public GenerateurEnquetes(RandomGenerator hasard, Trajets trajets, AnalyseEnquete analyse) {
        this.hasard = hasard; this.trajets = trajets; this.analyse = analyse;
    }
    public synchronized Partie creer(Partie.Mode mode) {
        var suspects = List.of(new Partie.Suspect("s1", "Alice Morel", "Restauratrice"),
                new Partie.Suspect("s2", "Bruno Lambert", "Collectionneur"),
                new Partie.Suspect("s3", "Camille Roche", "Archiviste"),
                new Partie.Suspect("s4", "David Armand", "Gardien"));
        String lieu = Carte.LIEUX.get(hasard.nextInt(Carte.LIEUX.size())).id();
        String coupable = suspects.get(hasard.nextInt(suspects.size())).id();
        var loin = Carte.LIEUX.stream().filter(l -> trajets.duree(l.id(), lieu) > 10).toList();
        var proche = Carte.LIEUX.stream().filter(l -> trajets.duree(l.id(), lieu) <= 10).toList();
        var observations = new LinkedHashMap<String, Partie.Presence>();
        for (var suspect : suspects) {
            var lieux = suspect.id().equals(coupable) ? proche : loin;
            observations.put(suspect.id(), new Partie.Presence(lieux.get(hasard.nextInt(lieux.size())).id(), 1190));
        }
        var partie = new Partie(UUID.randomUUID().toString(), mode, Partie.Etat.EN_COURS, 0,
                lieu, 1200, coupable, suspects, observations, Map.of(), Map.of(),
                "Le conservateur a été retrouvé sans vie à 20 h. Recoupe les présences et les trajets.");
        var verification = analyse.verifierGeneration(partie);
        if (!verification.coupableUnique() || !verification.coupablesIdentifies().contains(coupable)) {
            throw new IllegalStateException("L’enquête générée n’a pas de coupable unique vérifié");
        }
        return partie;
    }
}
