# MINDCRIME — démonstration

Prototype jouable pour explorer une idée de jeu d’enquête. Ce dépôt est indépendant du [projet universitaire MINDCRIME](https://github.com/salahbekkaripro/MINDCRIME) : il sert de référence et d’expérimentation, sans constituer une livraison officielle ou le travail personnel des cinq étudiants.

Le prototype a été développé avec Codex à la demande du propriétaire. Voir [l’usage de l’IA](AI_USAGE.md).

## Essayer

Prérequis : JDK 21 avec `JAVA_HOME` configuré, Node.js 20.19+ ou 22.12+, npm et Bash. Internet est nécessaire pour le premier téléchargement des dépendances.

```bash
git clone https://github.com/salahbekkaripro/MINDCRIME-demo.git
cd MINDCRIME-demo
./outils/lancer-demo.sh
```

Ouvrir **http://localhost:8080**. Arrêter avec `Ctrl+C`. Les parties sont conservées localement dans `.data/`, hors de Git. Un JDK n’est pas inclus dans le dépôt.

## Ce que l’on peut tester

- **Enquêteur** : interroger quatre suspects, découvrir leurs présences, lire les déductions temporelles puis identifier le coupable dans un modèle simplifié.
- **Alibi** : saisir et modifier les témoignages depuis une console partagée, puis explorer les scénarios compatibles. Les conclusions restent conditionnelles à la véracité des déclarations.
- Reprise des parties enregistrées et actualisation entre deux onglets par WebSocket.

## Architecture

React / TypeScript → API REST Spring Boot → moteur Java 21 → JPA / Flyway / H2.

Les algorithmes restent en Java : génération paramétrique, Dijkstra, recherche d’hypothèses et élimination. Le frontend affiche les résultats. Le moteur fonctionne sans API d’IA.

Le chemin local avec H2 a été vérifié : **24 tests Java réussis**, build TypeScript/Vite et parcours navigateur des deux modes. Aucun résultat de couverture ou de CI n’est revendiqué.

Les fichiers Docker Compose et le profil PostgreSQL sont fournis, mais leur exécution n’a pas été vérifiée. La console ne propose ni authentification ni rôles privés : cette démonstration est destinée à un usage local.

## Documentation

- [Guide complet : lancement, règles, API et limites](DEMO.md)
- [Backend et tests](backend/README.md)
- [Frontend](frontend/README.md)
- [Traçabilité IA](AI_USAGE.md)

Licence : à définir. Aucun badge de licence, de couverture ou de CI n’annonce de résultat non établi.
