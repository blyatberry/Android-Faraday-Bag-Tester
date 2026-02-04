# Faraday Bag Tester - Android App

Eine Android-App zum Testen der Wirksamkeit von Faraday-Beuteln durch Messung von Mobilfunk-, WLAN- und Bluetooth-Signalen.

## Funktionen

- **Mobilfunk-Signalmessung**: Überwacht die Signalstärke des Mobilfunknetzes (2G/3G/4G/5G)
- **WLAN-Signalmessung**: Misst die Stärke der WLAN-Verbindung
- **Bluetooth-Signalmessung**: Prüft den Bluetooth-Status und -Konnektivität
- **Echtzeit-Grafik**: Zeigt den Verlauf aller drei Signale in einem Live-Diagramm
- **Statusanzeige**: Bewertet die Gesamtabschirmung (Perfekt bis Schwach)
- **Intuitive Benutzeroberfläche**: Klare Darstellung mit Fortschrittsbalken und Farbcodierung

## Anforderungen

- Android 7.0 (API Level 24) oder höher
- Berechtigungen:
  - Standort (für WLAN-Scanning)
  - Telefonstatus (für Mobilfunkmessung)
  - Bluetooth (für Bluetooth-Messung)

## Installation & Verwendung

### In Android Studio:

1. Öffnen Sie das Projekt in Android Studio
2. Synchronisieren Sie die Gradle-Dateien
3. Verbinden Sie ein Android-Gerät oder starten Sie einen Emulator
4. Klicken Sie auf "Run" (grüner Play-Button)

### APK erstellen:

1. In Android Studio: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
2. Die APK finden Sie unter: `app/build/outputs/apk/debug/app-debug.apk`
3. Übertragen Sie die APK auf Ihr Android-Gerät und installieren Sie sie

### Verwendung der App:

1. **Start**: Öffnen Sie die App und erteilen Sie die erforderlichen Berechtigungen
2. **Referenzmessung**: Tippen Sie auf "Start" außerhalb des Faraday-Beutels, um Referenzwerte zu erhalten
3. **Test**: Legen Sie das Gerät in den Faraday-Beutel
4. **Auswertung**: Beobachten Sie, wie die Signalstärken auf 0 fallen sollten
5. **Ergebnis**: Der Status zeigt "✓ Perfekte Abschirmung" bei vollständiger Blockierung

## Interpretation der Ergebnisse

- **✓ Perfekte Abschirmung** (alle Signale = 0): Faraday-Beutel funktioniert einwandfrei
- **✓ Sehr gute Abschirmung** (1-3 Punkte): Minimale Signale, gute Abschirmung
- **⚠ Mäßige Abschirmung** (4-6 Punkte): Teilweise Abschirmung, Beutel könnte defekt sein
- **✗ Schwache Abschirmung** (7+ Punkte): Unzureichende Abschirmung, Beutel nicht effektiv

## Technische Details

### Projekt-Struktur:
```
FaradayBagTester/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/faradaybagtester/
│   │       │   ├── MainActivity.kt
│   │       │   └── SignalGraphView.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   └── values/
│   │       │       ├── strings.xml
│   │       │       ├── colors.xml
│   │       │       └── themes.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

### Verwendete Technologien:

- **Sprache**: Kotlin
- **UI**: Material Design Components
- **View Binding**: Für sichere View-Referenzen
- **Custom Views**: SignalGraphView für Echtzeit-Grafik
- **Android APIs**:
  - TelephonyManager (Mobilfunk)
  - WifiManager (WLAN)
  - BluetoothManager (Bluetooth)

### Signal-Messung:

- **Mobilfunk**: Nutzt `PhoneStateListener` für Echtzeit-Updates
- **WLAN**: Liest RSSI-Werte und konvertiert zu 0-4 Skala
- **Bluetooth**: Prüft Aktivierungsstatus und verbundene Geräte

## Hinweise

- Die Bluetooth-Messung ist eine Näherung, da Android keine direkten RSSI-Werte für Bluetooth ohne aktive Verbindung liefert
- Für beste Ergebnisse sollte GPS/Standort aktiviert sein
- Die App aktualisiert Werte jede Sekunde
- Das Grafikdiagramm zeigt die letzten 50 Messpunkte

## Lizenz

Dieses Projekt wurde als Demonstrationsprojekt erstellt. Frei verwendbar für eigene Zwecke.

## Support

Bei Fragen oder Problemen können Sie:
- Die Berechtigungen in den Android-Einstellungen überprüfen
- Das Gerät neu starten
- Die App neu installieren

---

**Wichtig**: Diese App dient nur zur Überprüfung der Abschirmungseigenschaften von Faraday-Beuteln. Sie ist kein Sicherheitstool und garantiert keine vollständige Privatsphäre.
