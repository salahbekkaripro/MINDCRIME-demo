package fr.miage.mindcrime.prototype;
import fr.miage.mindcrime.moteur.modele.*;
import fr.miage.mindcrime.moteur.chronologies.Trajets;
import fr.miage.mindcrime.moteur.contraintes.AnalyseEnquete;
import fr.miage.mindcrime.moteur.generation.GenerateurEnquetes;
import fr.miage.mindcrime.api.rest.VuePartie;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReglesJeuTest {
    private final Trajets trajets = new Trajets();
    private final AnalyseEnquete analyse = new AnalyseEnquete(trajets);
    private Partie partie(Partie.Mode mode, int seed) {
        return new GenerateurEnquetes(new Random(seed), trajets, analyse).creer(mode);
    }
    @Test void trajetsUtilisentLesCheminsIndirectsEtSontSymetriques() {
        assertEquals(44, trajets.duree("bibliotheque", "atelier"));
        assertEquals(44, trajets.duree("atelier", "bibliotheque"));
        assertEquals(0, trajets.duree("hall", "hall"));
        assertThrows(IllegalArgumentException.class, () -> trajets.duree("inconnu", "hall"));
    }
    @Test void centEnquetesGarantissentUnCoupableUniqueSeulementAvecLesIndicesDecouverts() {
        for (int seed = 0; seed < 100; seed++) {
            var p = partie(Partie.Mode.ENQUETEUR, seed);
            assertEquals(4, analyse.analyser(p).coupablesIdentifies().size());
            assertTrue(VuePartie.depuis(p, analyse.analyser(p)).suspects().stream().allMatch(s -> s.presence() == null));
            for (var s : p.suspects()) { p = p.interroger(s.id()); }
            var resultat = analyse.analyser(p);
            assertTrue(resultat.coupableUnique());
            assertEquals(Set.of(p.coupableId()), resultat.coupablesIdentifies());
        }
    }
    @Test void declarationMensongereChangeLesScenariosSansModifierLaVeriteInterne() {
        var p = partie(Partie.Mode.ALIBI, 4);
        String coupable = p.coupableId();
        var suspect = p.suspects().getFirst();
        String loin = Carte.LIEUX.stream().filter(l -> trajets.duree(l.id(), p.lieuCrime()) > 10).findFirst().orElseThrow().id();
        var modifiee = p.temoigner(suspect.id(), new Partie.Presence(loin, 1190));
        assertFalse(analyse.analyser(modifiee).coupablesIdentifies().contains(suspect.id()));
        var corrigee = modifiee.temoigner(suspect.id(), new Partie.Presence(p.lieuCrime(), 1190));
        assertTrue(analyse.analyser(corrigee).coupablesIdentifies().contains(suspect.id()));
        assertEquals(coupable, corrigee.coupableId());
        assertTrue(p.declarations().isEmpty());
    }
    @Test void lesPartiesTermineesEtLesEntreesInvalidesSontRefusees() {
        var p = partie(Partie.Mode.ENQUETEUR, 2);
        var fermee = p.terminer(Partie.Etat.RESOLUE, "terminée");
        assertThrows(Partie.PartieFermee.class, () -> fermee.interroger("s1"));
        assertThrows(Partie.PartieFermee.class, () -> p.temoigner("s1", new Partie.Presence("hall",1190)));
        assertThrows(IllegalArgumentException.class, () -> p.interroger("inconnu"));
        assertThrows(IllegalArgumentException.class, () -> new Partie.Presence("hall", 1201));
        assertThrows(IllegalArgumentException.class, () -> new Partie.Presence("inconnu", 1190));
    }
}
