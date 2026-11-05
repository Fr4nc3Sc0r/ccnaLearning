#!/usr/bin/env sh
# Compila (se necessario) e avvia l'applicazione.
cd "$(dirname "$0")"
[ -f dist/ccna-examlab.jar ] || ./build.sh
exec java -jar dist/ccna-examlab.jar "$@"
