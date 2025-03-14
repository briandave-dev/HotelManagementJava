@echo off
setlocal EnableDelayedExpansion

REM Définir le chemin vers le JAR (ajustez selon votre structure)
set JAR_PATH=HotelApp.jar

REM Définir le chemin vers le dossier des bibliothèques
set LIB_PATH=lib

REM Vérifier si le fichier JAR existe
if not exist "%JAR_PATH%" (
    echo Erreur: Le fichier %JAR_PATH% n'existe pas!
    exit /b 1
)

REM Vérifier si le dossier des bibliothèques existe
if not exist "%LIB_PATH%" (
    echo Avertissement: Le dossier %LIB_PATH% n'existe pas ou n'est pas accessible.
    echo Exécution sans bibliothèques externes...
    java -jar "%JAR_PATH%"
) else (
    REM Construire le classpath avec toutes les bibliothèques
    set CP=%JAR_PATH%
    for %%F in ("%LIB_PATH%\*.jar") do (
        set CP=!CP!;%%F
    )
    
    REM Exécuter l'application
    echo Démarrage de l'application...
    java -cp "!CP!" com.hotel.Main
)

endlocal