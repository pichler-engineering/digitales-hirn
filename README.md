# Digitales Hirn (Version 0.1)

Digitales Hirn ist eine lokale Android-App für Aufgaben, Notizen, Inbox-Gedanken und Erinnerungen. Die App funktioniert offline und speichert alle Daten dauerhaft auf dem Gerät (Room Database), ohne Login, Cloud-Konto oder externen Server.

## 1) Beschreibung der App

Die App bündelt persönliche Organisation in einer dunklen, einfachen Oberfläche:
- **Startseite** mit den Bereichen Gedanke, Heute, Projekte und Inbox
- **Spracheingabe (Deutsch)** für schnelle Erfassung
- **Aufgabenverwaltung** inklusive Priorität, Termin und Erinnerung
- **Notizen** mit Suche
- **Projektverwaltung** zur Strukturierung aller Einträge

## 2) Enthaltene Funktionen

- Kotlin + Jetpack Compose + Material 3
- Einfache MVVM-Struktur
- Lokale Room-Datenbank
- Aufgaben:
  - erstellen, bearbeiten, löschen
  - als erledigt markieren und wieder öffnen
  - Priorität: Niedrig / Normal / Hoch
  - Datum, Uhrzeit, Projekt, Beschreibung, Erinnerung
- Inbox:
  - schnelle Gedanken/Spracheingaben
  - bearbeiten, löschen, in Aufgabe umwandeln
- Heute-Ansicht:
  - heute fällige Aufgaben
  - überfällige offene Aufgaben
- Projekte:
  - erstellen, umbenennen, löschen, öffnen
  - automatische Beispielprojekte beim ersten Start
- Notizen:
  - erstellen, bearbeiten, löschen
  - durchsuchen
- Erinnerungen:
  - lokale Android-Benachrichtigungen
  - Notification Channel
  - Android-13-Berechtigung für Notifications
  - Wiederherstellung geplanter Erinnerungen nach Neustart (Boot Receiver)

## 3) Anleitung zum automatischen APK-Bau

Im Repository ist ein GitHub-Workflow vorhanden:
- Datei: `.github/workflows/android-build.yml`
- Triggert bei:
  - Push auf `main`
  - Pull Requests

Der Workflow führt aus:
1. Checkout
2. Java 17 Setup
3. Gradle Cache Setup
4. `./gradlew assembleDebug`
5. Upload der Debug-APK als Artifact

## 4) APK aus GitHub Actions herunterladen

1. Repository auf GitHub öffnen
2. Tab **Actions** öffnen
3. Einen erfolgreichen Lauf von **Android Build** auswählen
4. Im Bereich **Artifacts** das Artifact **digitales-hirn-debug-apk** herunterladen
5. ZIP entpacken → enthält `app-debug.apk`

## 5) Installation auf einem Samsung-Android-Handy

1. `app-debug.apk` auf das Samsung-Gerät übertragen (z. B. per USB, Cloud-Speicher oder Messenger)
2. Am Gerät in den Einstellungen die Installation aus unbekannten Quellen für die verwendete App erlauben (z. B. Dateien-App/Browser)
3. APK antippen und installieren
4. Nach der Installation App starten
5. Bei Nachfrage Berechtigungen für Mikrofon bzw. Benachrichtigungen passend freigeben

## 6) Hinweis zu Version 0.1

Dies ist **Version 0.1** mit Fokus auf eine sofort nutzbare lokale Basisfunktionalität. Alle Daten bleiben lokal auf dem Gerät gespeichert.
