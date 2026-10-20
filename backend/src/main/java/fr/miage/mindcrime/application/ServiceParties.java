package fr.miage.mindcrime.application;

import fr.miage.mindcrime.application.port.StockageParties;
import fr.miage.mindcrime.moteur.generation.GenerateurEnquetes;
import fr.miage.mindcrime.moteur.contraintes.AnalyseEnquete;
import fr.miage.mindcrime.moteur.deduction.ResultatDeduction;
import fr.miage.mindcrime.moteur.modele.Partie;
import java.util.List;
import java.util.NoSuchElementException;

public final class ServiceParties {
    private final StockageParties stockage;
    private final GenerateurEnquetes generateur;
    private final AnalyseEnquete analyse;
    public ServiceParties(StockageParties stockage, GenerateurEnquetes generateur, AnalyseEnquete analyse) {
        this.stockage = stockage; this.generateur = generateur; this.analyse = analyse;
    }
    public Partie creer(Partie.Mode mode) { return stockage.creer(generateur.creer(mode)); }
    public Partie lire(String id) { return stockage.lire(id).orElseThrow(() -> new NoSuchElementException("Partie introuvable")); }
    public List<Partie> recentes() { return stockage.recentes(); }
    public Partie interroger(String id, String suspect) { return stockage.modifier(id, p -> p.interroger(suspect)); }
    public Partie temoigner(String id, String suspect, Partie.Presence presence) {
        return stockage.modifier(id, p -> p.temoigner(suspect, presence));
    }
    public ResultatDeduction analyser(Partie partie) { return analyse.analyser(partie); }
    public Partie accuser(String id, String suspect) {
        return stockage.modifier(id, p -> {
            p.verifierEnCours(); p.suspect(suspect);
            if (p.mode() != Partie.Mode.ENQUETEUR) { throw new Partie.PartieFermee("Action réservée au mode Enquêteur"); }
            var deductions = analyse.analyser(p);
            if (!deductions.coupableUnique()) { return p.message("Les indices accessibles ne permettent pas encore une accusation certaine."); }
            if (!p.coupableId().equals(suspect)) { return p.message("Cette accusation ne concorde pas avec les indices. Continue l’enquête."); }
            return p.terminer(Partie.Etat.RESOLUE, "Enquête résolue : " + p.suspect(suspect).nom()
                    + " est le seul suspect dont la présence reste compatible avec les faits de cette énigme.");
        });
    }
    public Partie conclure(String id) {
        return stockage.modifier(id, p -> {
            p.verifierEnCours();
            if (p.mode() != Partie.Mode.ALIBI) { throw new Partie.PartieFermee("Action réservée au mode Alibi"); }
            if (p.declarations().size() != p.suspects().size()) { throw new Partie.PartieFermee("Chaque suspect doit d’abord témoigner"); }
            var resultat = analyse.analyser(p);
            String message = resultat.coupableUnique()
                    ? "Selon les déclarations, un seul suspect reste compatible. Cela ne prouve pas que les témoignages sont vrais."
                    : resultat.scenariosCompatibles().isEmpty()
                    ? "Aucun suspect ne peut atteindre la scène selon ces déclarations : l’ensemble ne permet pas de reconstruire le crime."
                    : "Plusieurs suspects restent compatibles : le détective ne peut pas conclure.";
            return p.terminer(Partie.Etat.TERMINEE, message);
        });
    }
}
