# Tester le prototype MINDCRIME

Prototype local demandé pour explorer les deux modes de jeu. Il est assisté par IA et ne constitue pas une release universitaire finale ni le bilan des contributions personnelles des cinq étudiants.

## Lancement simple

Depuis la racine du dépôt, sous Linux ou macOS avec Bash, Node.js 20.19+ (ou 22.12+) et un JDK 21 :

```bash
./outils/lancer-demo.sh
```

Ouvrir **http://localhost:8080**. Le script installe les dépendances front, construit React/TypeScript, compile et teste Java, puis démarre le JAR qui sert l’interface et l’API sur le même port. Les téléchargements nécessitent Internet au premier lancement. Arrêt : `Ctrl+C`.

Sur le poste de préparation, `.tools/jdk-21` est utilisé automatiquement s’il existe. Sinon configurer `JAVA_HOME` vers son JDK 21. Aucun changement de Java système n’est réalisé. Les caches `.m2-local`, `.npm-cache` et `.tools` restent hors de Git.

Si 8080 est occupé :

```bash
PORT=8081 ./outils/lancer-demo.sh
```

Pour relancer un build existant sans reconstruction :

```bash
export JAVA_HOME="$PWD/.tools/jdk-21" # ou le chemin de son JDK 21
"$JAVA_HOME/bin/java" -jar backend/target/mindcrime-backend-0.0.1.jar
```

Le serveur écoute uniquement en local par défaut. Les parties sont conservées dans `.data/mindcrime.mv.db`, relativement au répertoire de lancement. Lancer depuis la racine pour retrouver les mêmes données. Ne pas modifier ni supprimer la base pendant que le serveur l’utilise.

## Mode Enquêteur

1. Choisir « À toi de déduire ».
2. Lire le lieu et l’heure du crime, puis les temps de trajet sur la carte.
3. Interroger les quatre suspects. Chaque interrogation révèle une présence recoupée et vérifiée dans cette énigme.
4. Lire les éliminations calculées par Dijkstra puis le moteur D5. Une présence trop éloignée ne permet pas d’arriver à temps sur la scène.
5. Quand un seul suspect reste compatible, l’accuser. La partie passe à `RESOLUE` ; les modifications sont alors refusées.

L’enquête est générée avec un coupable caché et des présences compatibles ou incompatibles, puis revérifiée par le moteur commun. Au départ, l’API ne fournit ni les observations cachées ni le coupable : les quatre suspects restent possibles. La génération varie le lieu, le coupable et les présences sur une carte fixe ; ce n’est pas encore une génération narrative générale.

Hypothèses du modèle : ensemble fermé de quatre suspects, crime à 20 h, durées minimales fixes, aucune téléportation ou passage secret, présences vérifiées fiables dans ce mode. L’unicité est garantie dans ce modèle simplifié, pas pour toutes les enquêtes imaginables.

## Mode Alibi

1. Choisir « À toi de témoigner ».
2. Incarner les suspects depuis la console partagée : saisir un lieu et une heure entre 19 h et 20 h pour chacun.
3. Enregistrer les témoignages, puis observer les scénarios compatibles et les incompatibilités temporelles.
4. Modifier une déclaration pour tester un mensonge ou corriger une incohérence.
5. Après les quatre témoignages, conclure l’interrogatoire. Le moteur peut conserver plusieurs scénarios ou n’en trouver aucun ; il ne force pas une accusation.

Exemples : déclarer tout le monde sur la scène à 19 h 50 conserve quatre suspects ; placer un suspect trop loin à 19 h 50 l’exclut **à condition de croire sa déclaration**. Ces déclarations peuvent être fausses : l’analyse ne démontre pas leur vérité.

C’est une **console locale partagée sans authentification**. Chacun peut choisir n’importe quel suspect et modifier sa déclaration. Elle ne fournit pas de rôles privés ou de comptes sécurisés. Pour tester le temps réel, ouvrir le lien du même dossier dans deux onglets : les modifications apparaissent par WebSocket. La liste d’accueil permet de reprendre les vingt dossiers les plus récents.

## Architecture effectivement utilisée

- React + TypeScript : affichage, formulaires, carte SVG ; aucun calcul métier côté navigateur.
- Spring MVC : API REST et validation ; DTO public distinct de l’état interne.
- Java pur : génération paramétrique, règles d’états, Dijkstra, compatibilité temporelle et déduction.
- JPA / Flyway : table versionnée `parties_demo`, stockant des instantanés JSON avec révision et verrouillage optimiste.
- H2 fichier par défaut ; profil PostgreSQL et pilote présents.
- WebSocket : notification d’actualisation ; les données sont rechargées par REST, pas diffusées en clair sur le canal.

La table JSON est un choix de prototype, pas le schéma métier normalisé final prévu pour les features individuelles. Le moteur ne dépend d’aucune API d’IA.

## API de démonstration

| Méthode et chemin | Comportement |
| --- | --- |
| `POST /api/v1/parties` | Créer avec `{"mode":"ENQUETEUR"}` ou `{"mode":"ALIBI"}` ; 201 et Location |
| `GET /api/v1/parties` | Vingt dernières parties, vues publiques uniquement |
| `GET /api/v1/parties/{id}` | Relire le dossier et son analyse accessible |
| `POST /api/v1/parties/{id}/interrogatoires/{suspect}` | Découvrir la présence d’un suspect, mode Enquêteur |
| `PUT /api/v1/parties/{id}/temoignages/{suspect}` | Saisir `{"lieuId":"hall","minute":1190}` en mode Alibi |
| `POST /api/v1/parties/{id}/accusations` | Soumettre `{"suspectId":"s1"}` en mode Enquêteur |
| `POST /api/v1/parties/{id}/conclusion` | Terminer le mode Alibi après quatre témoignages |
| `WS /ws/parties/{id}` | Recevoir un signal d’actualisation |

Minutes depuis minuit : 1190 = 19 h 50. Les identifiants des lieux et suspects sont fournis par la vue de partie. Erreurs : 400 entrée invalide, 404 partie inconnue, 409 action incompatible avec l’état/mode ou modification concurrente. OpenAPI et Swagger UI ne sont pas configurés pour ce prototype.

## Développement et vérifications

Frontend seul avec rechargement automatique (backend sur 8080) :

```bash
cd frontend
npm ci
npm run dev
```

Vite redirige `/api` et `/ws` vers le backend ; ouvrir l’URL affichée par Vite. Après modification du front, reconstruire celui-ci puis le JAR pour le lancement tout-en-un.

Depuis la racine, avec Java 21 configuré :

```bash
./backend/mvnw -f backend/pom.xml verify
npm --prefix frontend run build
```

Si les caches locaux sont utilisés : définir `MAVEN_USER_HOME="$PWD/.m2-local"` et ajouter `-Dmaven.repo.local="$PWD/.m2-local"` à Maven.

Vérifications réellement effectuées : **24 tests Java sans échec**, dont les 19 tests D5, quatre tests des règles de jeu (incluant 100 générations reproductibles) et un test d’intégration qui lance deux contextes HTTP avec H2, Flyway et JPA. Ce dernier couvre les deux modes, des erreurs, la protection des données cachées et la relecture des données dans le second contexte. Le build TypeScript/Vite a réussi.

Des essais automatisés avec Chrome headless ont aussi exécuté les deux parcours, la synchronisation entre deux onglets et un affichage mobile de 390 px, sans erreur JavaScript ni débordement horizontal. L’outil Playwright de contrôle est local à `.tools`, pas une dépendance publiée du projet. Ce bilan ne vaut pas score de couverture, conformité Checkstyle ou réussite d’une CI : ces outils ne sont pas configurés.

## Docker et PostgreSQL : chemin alternatif fourni

Avec Docker et le plugin Compose installés :

```bash
docker compose up --build
```

L’interface est exposée uniquement sur http://localhost:8080 ; PostgreSQL reste dans le réseau Compose et ses données utilisent un volume nommé. Le mot de passe par défaut `mindcrime-demo-local` est une valeur de démonstration ; `DB_PASSWORD` permet de le remplacer. Ce chemin nécessite les téléchargements des images et dépendances.

**Limite de vérification :** Docker Compose est absent du poste de préparation. Le Dockerfile, le profil PostgreSQL et le fichier Compose sont fournis, mais leur construction et l’exécution PostgreSQL n’ont pas été testées. Le chemin vérifié est le lancement local avec H2. Ne pas annoncer Docker/PostgreSQL comme validés.

## Suite du projet universitaire

Les quinze fonctionnalités majeures, la difficulté paramétrable, les noyaux minimaux de contradictions, les arbres de décision avancés, les explications avancées, l’authentification, le schéma normalisé, OpenAPI, les contrôles qualité, la CI et les publications restent des travaux à poursuivre. Le prototype permet de tester le concept de bout en bout ; il ne remplace pas ces livraisons ni les revues et contributions des membres.

Ce dépôt de démonstration possède un historique indépendant du dépôt universitaire. Il ne contient ni le dossier R1 ni les tags de livraison universitaires. Sa publication a été demandée explicitement par le propriétaire.
