# Interface de démonstration MINDCRIME

React + TypeScript, build Vite. Le moteur reste en Java ; la carte affiche les durées fournies par le backend et les formulaires envoient les actions à l’API.

```bash
npm ci
npm run dev
```

Backend requis sur `127.0.0.1:8080`. Vite redirige les requêtes REST et WebSocket ; aucun réglage CORS large n’est ajouté au backend. Le build `npm run build` vérifie TypeScript et génère `dist/`, intégré au JAR lors d’un build Maven ultérieur.

Pour lancer tout le projet depuis la racine : `./outils/lancer-demo.sh`. Consulter [DEMO.md](../DEMO.md) pour les parcours, les tests et les limites. Les polices système sont utilisées ; aucun téléchargement de police n’est nécessaire.
