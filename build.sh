#!/usr/bin/env sh
# Compila il progetto e crea dist/ccna-examlab.jar usando solo il JDK (nessun Maven richiesto).
set -e
cd "$(dirname "$0")"

OUT=build/classes
rm -rf build dist
mkdir -p "$OUT" dist

echo "Compilazione sorgenti..."
find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d "$OUT" @build/sources.txt

echo "Copia risorse..."
cp -R src/main/resources/. "$OUT"/

echo "Creazione JAR..."
jar --create --file dist/ccna-examlab.jar --main-class com.ccnaexamlab.app.Main -C "$OUT" .

echo "Verifica banca domande..."
java -jar dist/ccna-examlab.jar --validate

echo "Fatto: dist/ccna-examlab.jar  (avvio: java -jar dist/ccna-examlab.jar)"
