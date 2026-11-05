@echo off
REM Compila il progetto e crea dist\ccna-examlab.jar usando solo il JDK (nessun Maven richiesto).
setlocal
cd /d "%~dp0"

if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build\classes
mkdir dist

echo Compilazione sorgenti...
dir /s /b src\main\java\*.java > build\sources.txt
javac --release 17 -encoding UTF-8 -d build\classes @build\sources.txt || goto :error

echo Copia risorse...
xcopy /e /i /q src\main\resources build\classes >nul

echo Creazione JAR...
jar --create --file dist\ccna-examlab.jar --main-class com.ccnaexamlab.app.Main -C build\classes . || goto :error

echo Verifica banca domande...
java -jar dist\ccna-examlab.jar --validate || goto :error

echo Fatto: dist\ccna-examlab.jar  (avvio: java -jar dist\ccna-examlab.jar)
goto :eof

:error
echo Build fallita.
exit /b 1
