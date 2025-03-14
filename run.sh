#!/bin/bash

# Définir le chemin vers le JAR (ajustez selon votre structure)
JAR_PATH="HotelApp.jar"

# Définir le chemin vers le dossier des bibliothèques
LIB_PATH="lib"

# Vérifier si le fichier JAR existe
if [ ! -f "$JAR_PATH" ]; then
    echo "Erreur: Le fichier $JAR_PATH n'existe pas!"
    exit 1
fi

# Vérifier si le dossier des bibliothèques existe
if [ ! -d "$LIB_PATH" ]; then
    echo "Avertissement: Le dossier $LIB_PATH n'existe pas ou n'est pas accessible."
    echo "Exécution sans bibliothèques externes..."
    java -jar "$JAR_PATH"
else
    # Construire le classpath avec toutes les bibliothèques
    CP="$JAR_PATH"
    for jar in "$LIB_PATH"/*.jar; do
        CP="$CP:$jar"
    done
    
    # Exécuter l'application
    echo "Démarrage de l'application..."
    java -cp "$CP" com.hotel.Main
fi