package fr.miage.mindcrime.moteur.modele;

import java.util.List;

/** Carte de démonstration, trajets piétons non orientés en minutes. */
public final class Carte {
    public record Lieu(String id, String nom, int x, int y) { }
    public record Liaison(String depart, String arrivee, int minutes) { }
    public static final List<Lieu> LIEUX = List.of(
            new Lieu("bibliotheque", "Bibliothèque", 70, 90),
            new Lieu("galerie", "Galerie", 230, 70),
            new Lieu("hall", "Hall", 370, 170),
            new Lieu("jardin", "Jardin", 230, 290),
            new Lieu("atelier", "Atelier", 70, 270));
    public static final List<Liaison> LIAISONS = List.of(
            new Liaison("bibliotheque", "galerie", 12), new Liaison("galerie", "hall", 8),
            new Liaison("hall", "jardin", 10), new Liaison("jardin", "atelier", 14));
    private Carte() { }
    public static Lieu lieu(String id) {
        return LIEUX.stream().filter(lieu -> lieu.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lieu inconnu"));
    }
    public static String heure(int minute) {
        return "%02d:%02d".formatted(minute / 60, minute % 60);
    }
}
