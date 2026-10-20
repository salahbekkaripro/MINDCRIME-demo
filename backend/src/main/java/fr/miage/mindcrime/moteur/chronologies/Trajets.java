package fr.miage.mindcrime.moteur.chronologies;

import fr.miage.mindcrime.moteur.modele.Carte;
import java.util.*;

/** Dijkstra sur un graphe non orienté à poids positifs. */
public final class Trajets {
    private record Etape(String lieu, int duree) { }
    public int duree(String depart, String arrivee) {
        Carte.lieu(depart); Carte.lieu(arrivee);
        var distances = new HashMap<String, Integer>();
        var file = new PriorityQueue<Etape>(Comparator.comparingInt(Etape::duree));
        distances.put(depart, 0); file.add(new Etape(depart, 0));
        while (!file.isEmpty()) {
            var etape = file.remove();
            if (etape.duree() != distances.get(etape.lieu())) { continue; }
            if (etape.lieu().equals(arrivee)) { return etape.duree(); }
            for (var liaison : Carte.LIAISONS) {
                String voisin = liaison.depart().equals(etape.lieu()) ? liaison.arrivee()
                        : liaison.arrivee().equals(etape.lieu()) ? liaison.depart() : null;
                if (voisin == null) { continue; }
                int nouveau = etape.duree() + liaison.minutes();
                if (nouveau < distances.getOrDefault(voisin, Integer.MAX_VALUE)) {
                    distances.put(voisin, nouveau); file.add(new Etape(voisin, nouveau));
                }
            }
        }
        throw new IllegalStateException("Carte déconnectée");
    }
}
