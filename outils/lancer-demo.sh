#!/usr/bin/env bash
set -euo pipefail
PROJECT_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"
if [[ -x "$PROJECT_ROOT/.tools/jdk-21/bin/java" ]]; then
  export JAVA_HOME="$PROJECT_ROOT/.tools/jdk-21"
fi
if [[ -z "${JAVA_HOME:-}" || ! -x "$JAVA_HOME/bin/java" ]]; then
  echo 'Configure JAVA_HOME vers un JDK 21 avant de lancer la démo.' >&2
  exit 1
fi
export MAVEN_USER_HOME="$PROJECT_ROOT/.m2-local"
command -v npm >/dev/null || { echo 'Node.js 20.19+ et npm sont nécessaires.' >&2; exit 1; }
(cd frontend && npm ci --cache "$PROJECT_ROOT/.npm-cache" --no-audit --no-fund && npm run build)
./backend/mvnw -B -f backend/pom.xml -Dmaven.repo.local="$PROJECT_ROOT/.m2-local" verify
printf '\nMINDCRIME : http://localhost:%s\nArrêt : Ctrl+C. Parties enregistrées dans .data/.\n' "${PORT:-8080}"
exec "$JAVA_HOME/bin/java" -jar backend/target/mindcrime-backend-0.0.1.jar
