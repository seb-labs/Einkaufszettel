# Einkaufszettel

Eine schlichte, lokale Android-App für deinen normalen Wocheneinkauf.

## Funktionen
- mehrere Einkaufslisten
- Standardliste **Wocheneinkauf** beim ersten Start
- Artikel mit Menge und optionaler Kategorie
- Abhaken von Artikeln
- Bearbeiten und Löschen von Artikeln
- erledigte Artikel unten anzeigen oder ausblenden
- erledigte Artikel gesammelt entfernen
- häufige Artikel als Vorschläge in den Dialogen
- lokale Speicherung auf dem Gerät
- helles und dunkles Material-3-Design

## Screens und Navigation
- **Hauptscreen**: zeigt deine aktuell gewählte Liste, offene Artikel und optional erledigte Artikel
- **Listen-Auswahl**: über dein Listen-Menü im oberen Bereich
- **Dialog „Artikel hinzufügen“**: Name, Menge, Kategorie und Vorschläge
- **Dialog „Artikel bearbeiten“**: dieselben Felder wie beim Hinzufügen
- **Menü**: Liste anlegen, umbenennen, löschen, erledigte Artikel ausblenden/anzeigen, erledigte Artikel entfernen, Info

## Technischer Stack
- Kotlin
- Jetpack Compose
- Material 3
- Android Studio Projekt
- lokale JSON-Persistenz im internen App-Speicher

## Speicherlösung
Die App speichert ihre Daten lokal in einer JSON-Datei im internen Speicher des Geräts. Es gibt keine Cloud-Anbindung und keine Anmeldung.

## Datenschutz
Die App benötigt keine besonderen Berechtigungen und überträgt keine Nutzerdaten an externe Dienste.

## Build
```bash
./gradlew assembleDebug
```

## Projektstatus
- Grundgerüst angelegt
- Kernfunktionen implementiert
- Debug-Build und JVM-Tests erfolgreich geprüft
- passendes grün-natürliches Farbschema und Launcher-Icon gesetzt
- offene Punkte: optionales Feintuning von UI/Layouts nach realem Emulator- oder Gerätecheck

## Hinweis für Android Studio
Öffne einfach den Ordner `Einkaufszettel` als Projekt. Das Projekt ist als normales Android-Studio-Projekt mit einem App-Modul aufgebaut.
