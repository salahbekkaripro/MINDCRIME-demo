# Socle backend MINDCRIME

Le backend sert maintenant le prototype web : REST, WebSocket, JPA/Flyway et H2. Le moteur D5 reste en Java pur, réutilisé avec les règles de déplacement et les présences du prototype. Voir [DEMO.md](../DEMO.md) pour les fonctionnalités et limites.

## Prérequis

- JDK **21** : `JAVA_HOME` doit désigner le dossier du JDK, qui contient `bin/java` et `bin/javac`.
- Accès réseau lors du premier build pour récupérer Maven et les dépendances.
- Spring Boot **4.0.8**, version fixée dans `pom.xml`.
- Maven **3.9.11**, téléchargé et utilisé par le Maven Wrapper officiel **3.3.4** ; aucun Maven système n'est nécessaire après génération du wrapper.

## Compiler depuis la racine du dépôt

Configurer `JAVA_HOME` pour son propre JDK 21, puis exécuter :

```bash
./backend/mvnw -f backend/pom.xml verify
```

Sous Windows, utiliser `backend\mvnw.cmd -f backend\pom.xml verify` après configuration de `JAVA_HOME`. Cette variante n'a pas été exécutée dans l'environnement Linux de préparation.

Dans l'environnement de cette session, Java 21 a été téléchargé dans `.tools/jdk-21`, ignoré par Git. Pour utiliser ce JDK et garder aussi le cache Maven dans le projet :

```bash
export JAVA_HOME="$PWD/.tools/jdk-21"
export MAVEN_USER_HOME="$PWD/.m2-local"
./backend/mvnw -f backend/pom.xml -Dmaven.repo.local="$PWD/.m2-local" verify
```

Ces variables concernent le terminal courant ; le Java du système et les fichiers de profil ne sont pas modifiés. Le JDK local n'est pas livré avec le dépôt : sur un autre poste, installer un JDK 21 puis pointer `JAVA_HOME` vers celui-ci. `.tools`, `.m2-local` et `target` sont ignorés par Git.

## Démarrer le socle compilé

Depuis la racine, avec `JAVA_HOME` correctement défini :

```bash
"$JAVA_HOME/bin/java" -jar backend/target/mindcrime-backend-0.0.1.jar
```

Le serveur reste à l’écoute sur `127.0.0.1:8080`. Pour obtenir aussi l’interface dans le JAR, construire d’abord `frontend` (`npm ci`, puis `npm run build`) et relancer Maven. Le script `outils/lancer-demo.sh` orchestre ces étapes. H2 enregistre les parties dans `.data/`, relativement au répertoire courant.

## État des vérifications

Le build `verify` exécute **24 tests JUnit 5** : 19 tests D5 sans Spring, quatre tests des règles de jeu et un parcours HTTP avec Flyway/JPA/H2. Ils vérifient notamment les solutions, les explications, les données immuables, les limites, les évaluations indéterminées et l’égalité avec une énumération exhaustive sur 60 cas à graine fixe. Couverture, contrôles d’architecture automatisés, Checkstyle et CI restent à configurer. Les règles temporelles du prototype sont intégrées ; cela ne signifie pas que les versions complètes des features universitaires D1–D4 sont réalisées ou revues.

Ce dépôt contient un prototype indépendant destiné à explorer le concept. Il ne constitue pas le bilan des contributions individuelles du projet universitaire. Les dépendances REST, validation, WebSocket, JPA, Flyway, H2 et PostgreSQL sont présentes pour le prototype.

Le wrapper provient de [l'outil officiel Apache Maven](https://maven.apache.org/tools/wrapper/). Les exigences de Spring Boot sont décrites dans la [documentation officielle](https://docs.spring.io/spring-boot/4.0/system-requirements.html).

## Tester le moteur D5

Avec `JAVA_HOME` configuré comme ci-dessus, depuis la racine :

```bash
./backend/mvnw -f backend/pom.xml -Dtest=MoteurDeductionTest test
```

Ajouter `-Dmaven.repo.local="$PWD/.m2-local"` si le cache local du projet est utilisé. Le compte rendu détaillé est produit dans `backend/target/surefire-reports/`.

Après compilation des tests, exécuter la démonstration sans dépendance à Spring :

```bash
"$JAVA_HOME/bin/java" -cp backend/target/classes:backend/target/test-classes \
  fr.miage.mindcrime.moteur.deduction.DemonstrationDeduction
```

Sous Windows, le séparateur du classpath est `;` au lieu de `:`. La démonstration utilise des règles fictives de test, pas les moteurs D2/D3 réels. Elle affiche :

- P1 et P2 présents : `SCENARIO_UNIQUE`, Bruno/bureau et les motifs d’élimination.
- P2 retiré : `PLUSIEURS_SCENARIOS`, Alice/bureau et Bruno/bureau.
- Budget d’un nœud : `INDETERMINE`, analyse incomplète et aucune accusation.

`MoteurDeduction` reçoit une requête et une liste d’`EvaluateurHypothese`. Les domaines D2/D3 devront fournir des implémentations qui respectent le contrat de vérification ; le moteur ne calcule pas lui-même les trajets et ne réimplémente pas leur propagation logique. Il vérifie que les références d’indices des motifs appartiennent au contexte autorisé ; la construction sûre de ce contexte et les droits restent la responsabilité du futur cas d’utilisation.

La recherche utilise une pile explicite, un chemin mutable restauré au retour arrière et des instantanés immuables pour les évaluateurs et résultats. Avec `N` nœuds visités, `v` variables et un coût d’évaluation `C`, le temps comprend `O(N × (C + v))`, dont la copie des instantanés. La pile et le chemin occupent `O(v)` hors résultats et mémoire des évaluateurs ; les scénarios, branches éliminées et motifs conservés peuvent occuper une mémoire exponentielle. Le budget porte sur les nœuds, pas sur le temps d’un évaluateur externe.
