# Projektstatus — Einkaufszettel

## Aktueller Stand
- Neues Android-Studio-Projekt unter `/home/seb/Schreibtisch/Einkaufszettel`
- Kotlin + Jetpack Compose + Material 3
- lokale JSON-Speicherung im internen App-Speicher
- mehrere Einkaufslisten, Artikelverwaltung, Abhaken, Bearbeiten, Löschen
- häufige Artikel als einfache lokale Vorschläge

## Getroffene Annahmen
- Für Version 1 reicht eine robuste lokale JSON-Datei statt Room oder DataStore.
- Kategorien bleiben als feste Standardliste erhalten; eine eigene Kategorienverwaltung ist vorerst nicht nötig.
- Erledigte Artikel werden per Menü entweder unten angezeigt oder ausgeblendet.

## Projektpfad
`/home/seb/Schreibtisch/Einkaufszettel`

## Verwendete Technologien
- Kotlin
- Android Gradle Plugin
- Jetpack Compose
- Material 3
- org.json für die lokale Dateiserialisierung

## Speicherentscheidung
- Eine interne JSON-Datei im App-Speicher (`shoppingzettel.json`)
- Vorteil: einfach, robust, kein Zusatz-Setup
- Nachteil: später weniger strukturiert als Room, aber für Version 1 ausreichend

## Bekannte Einschränkungen
- Keine Synchronisation zwischen Geräten
- Keine Cloud-/Login-Funktionen
- Keine Widget- oder Benachrichtigungslogik
- Build und Unit-Tests wurden im aktuellen Stand erfolgreich ausgeführt

## Nächste sinnvolle Schritte
1. UI in Android Studio auf einem Emulator oder Gerät ansehen
2. Bei Bedarf Layout, Abstände und Dialoge feinjustieren
3. Optional später Room-Migration, falls das Datenmodell wächst

## Hinweise für den Nutzer zum Testen in Android Studio
- Den Ordner in Android Studio öffnen
- Gradle-Sync abwarten
- App-Modul starten
- Wenn der Build grün ist, eine erste Debug-APK installieren oder direkt im Emulator starten
