# RadioBT

App Android per trasmettere musica via Bluetooth alle autoradio cinesi Android, con invio completo dei metadati (titolo, artista, album, copertina, numero traccia, durata) tramite protocollo **A2DP + AVRCP**.

---

## Perché esiste

Le autoradio Android cinesi (Carlinkit, Joying, ecc.) hanno app native pessime per la gestione della musica via Bluetooth. RadioBT usa i protocolli Bluetooth standard già supportati dall'autoradio — nessun driver aggiuntivo, nessuna configurazione: basta collegare il telefono e riprodurre.

---

## Funzionalità

| Feature | Dettaglio |
|---|---|
| **A2DP** | Streaming audio ad alta qualità verso l'autoradio |
| **AVRCP** | Metadati inviati automaticamente: titolo, artista, album, n° traccia, durata |
| **Cover art** | Copertina dell'album trasmessa all'autoradio |
| **Libreria locale** | Legge tutti i file audio dal dispositivo via MediaStore |
| **Tema AMOLED** | Sfondo nero puro (`#000000`) — risparmio batteria su display OLED |
| **Sfondo blur** | Copertina sfumata come sfondo della schermata player |
| **Animazione cover** | La copertina si allarga al play e si rimpicciolisce al pause |
| **Luminosità** | Slider in-app per regolare la luminosità della finestra (senza permessi speciali) |
| **Spegni schermo** | Overlay nero totale con tap per riattivare — musica e BT restano attivi |
| **Compatibilità** | Android 5.0+ (API 21) |

---

## Stack tecnico

| Componente | Tecnologia |
|---|---|
| Linguaggio | Kotlin |
| UI | Jetpack Compose + Material3 |
| Playback | AndroidX Media3 (ExoPlayer 1.4.1) |
| Bluetooth | `MediaSessionService` → Android Bluetooth stack → AVRCP/A2DP automatico |
| Immagini | Coil 2.7.0 (con `BlurTransformation` per lo sfondo) |
| Architettura | MVVM — `PlayerViewModel` + `StateFlow` |
| Min SDK | 21 (Android 5.0 Lollipop) |
| Target SDK | 35 (Android 15) |

---

## Come funziona il Bluetooth

Android gestisce A2DP e AVRCP in automatico tramite la classe `MediaSession`. RadioBT:

1. Avvia un `MediaLibraryService` con un `ExoPlayer` configurato per `USAGE_MEDIA`
2. Imposta i `MediaMetadata` su ogni `MediaItem` (titolo, artista, album, n° traccia, URI copertina)
3. Il sistema Android fa il bridge tra `MediaSession` e lo stack Bluetooth
4. L'autoradio riceve i metadati tramite AVRCP senza nessuna configurazione aggiuntiva

---

## Struttura del progetto

```
RadioBT/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/matteocandeloro/radiobt/
│           ├── MainActivity.kt          # Entry point, permessi, navigazione, brightness
│           ├── MusicService.kt          # MediaLibraryService (background playback + BT)
│           ├── MusicRepository.kt       # Lettura libreria da MediaStore
│           ├── PlayerViewModel.kt       # Stato player, controlli, polling posizione
│           ├── Track.kt                 # Data class (id, title, artist, album, uri, ...)
│           └── ui/
│               ├── MainActivity.kt      # (entry)
│               ├── LibraryScreen.kt     # Lista brani con mini-player in basso
│               ├── PlayerScreen.kt      # Schermata Now Playing full-screen
│               └── Theme.kt             # Tema AMOLED forzato dark
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
└── settings.gradle.kts

web/
└── index.html                           # Pagina di download APK (dark theme)

.github/
└── workflows/
    └── pages.yml                        # CI: build APK + deploy su server Hetzner
```

---

## Permessi richiesti

| Permesso | Motivo |
|---|---|
| `READ_MEDIA_AUDIO` (API 33+) | Leggere la libreria musicale |
| `READ_EXTERNAL_STORAGE` (API ≤ 32) | Leggere la libreria musicale (legacy) |
| `BLUETOOTH_CONNECT` (API 31+) | Connettersi all'autoradio |
| `BLUETOOTH` (API ≤ 30) | Connessione BT legacy |
| `FOREGROUND_SERVICE` | Tenere attivo il player in background |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Tipo specifico foreground service |

---

## Dipendenze principali

```toml
# gradle/libs.versions.toml
media3       = "1.4.1"    # ExoPlayer + MediaSession
coil         = "2.7.0"    # Caricamento immagini + blur
material3    = "1.3.1"    # UI components
composeBom   = "2024.12.01"
kotlin       = "2.0.21"
agp          = "8.7.3"
```

---

## Build

### Requisiti
- Android Studio Ladybug (2024.2) o superiore
- JDK 17
- Android SDK API 35

### Da Android Studio
1. `File → Open` → seleziona la cartella `RadioBT/`
2. Attendi la sync Gradle
3. `Build → Build APK` oppure esegui direttamente su dispositivo/emulatore

### Da riga di comando
```bash
cd RadioBT
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

---

## CI/CD

Il workflow `.github/workflows/pages.yml` si attiva ad ogni push su `main`:

1. Fa checkout del codice
2. Deploya `web/` sul server Hetzner via **rsync over SSH**

Secret GitHub richiesti nel repo:

| Secret | Contenuto |
|---|---|
| `SSH_PRIVATE_KEY` | Chiave SSH privata |
| `SSH_HOST` | IP o hostname del server |
| `SSH_USER` | Utente SSH (es. `root`) |
| `SSH_PORT` | Porta SSH (default: `22`) |
| `DEPLOY_PATH` | Percorso destinazione (default: `/var/www/html/radiobt`) |

---

## Installazione su dispositivo

1. Scarica l'APK dalla pagina web
2. **Impostazioni → Sicurezza → Origini sconosciute** (su Android 8+: *Installa app sconosciute*)
3. Apri il file APK e segui le istruzioni
4. Abbina il telefono all'autoradio via Bluetooth
5. Avvia RadioBT, seleziona un brano — i metadati arrivano in automatico sul display della radio
