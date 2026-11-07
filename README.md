# LINAS
Old strategy pvp game. After getting it up and running, adapt codebase to allow RL model training to create a game AI.

## Windows-EXE erstellen

Im Windows-Explorer `build-windows.cmd` doppelklicken. Alternativ im
IntelliJ-Terminal:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\build-windows.ps1
```

Das Skript kompiliert die Quellen, erstellt eine JAR und erzeugt mit
`jpackage` eine Windows-App inklusive Java-Laufzeit und Spieldaten.
Voraussetzung auf dem Build-Rechner: ein vollstaendiges JDK ab Version 17
mit `javac`, `jar` und `jpackage`. Gesucht wird ueber `JAVA_HOME`, PATH
und IntelliJs JDK-Downloads unter `%USERPROFILE%\.jdks`.
Ein JDK kann auch explizit angegeben werden:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\build-windows.ps1 -JdkHome "C:\Pfad\zum\jdk"
```

Ergebnis: `dist/build-<Zeitstempel>/windows/LINAS/LINAS.exe`.
Zum Weitergeben den **gesamten LINAS-Ordner** zippen; auf dem Zielrechner
ist keine Java-Installation erforderlich. Jeder Build bekommt einen neuen
Ordner. Alte Builds koennen bei Bedarf manuell geloescht werden.

In IntelliJ bleibt die Run-Konfiguration `Linas` nutzbar. Die Java-Quellen
verwenden Windows-1252 (entsprechend der bestehenden IntelliJ-Einstellung).
Bilder und Einheiten werden unabhaengig vom Arbeitsverzeichnis gefunden.
Netzwerk-Transferdateien liegen pro Prozess in einem eigenen temporaeren
Verzeichnis und werden beim normalen Beenden entfernt.
