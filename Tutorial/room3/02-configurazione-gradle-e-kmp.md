# Capitolo 2: Configurazione Gradle, KSP e Kotlin Multiplatform

Configurare le dipendenze in un progetto **Kotlin Multiplatform (KMP)** con Room 3 è uno dei passaggi in cui i neofiti si bloccano più spesso. In questo capitolo analizziamo ogni singola riga di configurazione dei file di Gradle, spiegando **perché** è necessaria e quale compito svolge.

---

## 1. La Triade: Kotlin, KSP e Room 3

Prima di toccare il codice, è fondamentale capire come collaborano questi tre strumenti:

```
┌──────────────────┐
│ Compilatore      │ ◄─── Legge il tuo codice Kotlin normale
│ Kotlin           │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ KSP              │ ◄─── Legge le annotazioni (@Entity, @Dao, @Database)
│ (Symbol Proc.)   │      e invoca il compilatore di Room 3
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Room 3 Compiler  │ ◄─── Genera il codice sorgente Kotlin dei DAO
│                  │      e verifica le query SQL contro lo schema
└──────────────────┘
```

> [!IMPORTANT]
> **Regola aurea delle versioni:** La versione del plugin KSP **deve sempre corrispondere** alla versione del compilatore Kotlin. Ad esempio, se usi Kotlin `2.4.21`, la versione di KSP deve essere `2.4.21-1.0.xx` (o la release compatibile indicata sul repository ufficiale di KSP). Un disallineamento impedirà a Gradle di avviare la compilazione.

---

## 2. Il Version Catalog: `gradle/libs.versions.toml`

Nei progetti moderni, tutte le versioni e le dipendenze sono centralizzate nel file `libs.versions.toml`. Ecco la configurazione utilizzata in **FitSlot**:

```toml
[versions]
# Versioni base del linguaggio e del processore
kotlin = "2.4.21"
ksp = "2.3.10"               # Compatibile con Kotlin

# Versioni per Room 3 e SQLite
room3 = "3.1.0-alpha03"       # Versione di Room 3 con supporto KMP nativo
sqlite = "2.7.1"              # Driver SQLite multipiattaforma

[libraries]
# 1. Libreria a runtime di Room 3 (da usare nel nostro codice comune)
androidx-room3-runtime = { module = "androidx.room3:room3-runtime", version.ref = "room3" }

# 2. Compilatore di Room 3 (usato da KSP a tempo di build per generare il codice)
androidx-room3-compiler = { module = "androidx.room3:room3-compiler", version.ref = "room3" }

# 3. SQLite "bundled" (motore SQLite C precompilato per tutte le piattaforme)
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }

[plugins]
# Plugin per processare i simboli Kotlin
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

# Plugin ufficiale di Room 3 per gestire lo schema e la generazione
androidx-room3 = { id = "androidx.room3", version.ref = "room3" }
```

### Spiegazione degli artefatti

1. **`androidx.room3:room3-runtime`**: Contiene le classi che importi nei tuoi file Kotlin (come `RoomDatabase`, `@Entity`, `@Dao`, `@Query`, `@ColumnTypeConverter`).
2. **`androidx.room3:room3-compiler`**: Non finisce nell'applicazione finale! È l'eseguibile che gira sul tuo computer durante la compilazione per analizzare le tue query SQL e generare le classi di supporto.
3. **`androidx.sqlite:sqlite-bundled`**: Fornisce l'implementazione `BundledSQLiteDriver`. SQLite è scritto in linguaggio C. Invece di sperare che sul computer dell'utente (Windows 10, Windows 11, Ubuntu o macOS) sia installata la giusta versione di SQLite, questo pacchetto include una copia identica e collaudata della libreria C compilata per ogni sistema operativo.

---

## 3. Configurazione del Modulo Condiviso: `shared/build.gradle.kts`

Il modulo `shared` contiene sia il codice comune multipiattaforma (`commonMain`), sia la configurazione specifica per il desktop (`jvm`).

Ecco il file `shared/build.gradle.kts` commentato punto per punto:

```kotlin
plugins {
    // Abilita Kotlin Multiplatform
    alias(libs.plugins.kotlinMultiplatform)
    
    // Abilita il motore di generazione codice KSP
    alias(libs.plugins.ksp)
    
    // Abilita il plugin ufficiale di Room 3
    alias(libs.plugins.androidx.room3)
}

kotlin {
    // Definiamo il target JVM per l'applicazione desktop
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Room 3 Runtime: visibile a tutte le piattaforme (desktop, mobile, ecc.)
            implementation(libs.androidx.room3.runtime)
            
            // Driver SQLite bundled: per aprire le connessioni al database
            implementation(libs.androidx.sqlite.bundled)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// ⚠️ PUNTO CRITICO: Dove assegnare il compilatore Room
dependencies {
    // In un progetto KMP il compilatore KSP non si aggiunge con "ksp(...)",
    // ma specificando il target di compilazione, in questo caso "kspJvm"!
    add("kspJvm", libs.androidx.room3.compiler)
    
    // Se in futuro aggiungerai Android, scriverai:
    // add("kspAndroid", libs.androidx.room3.compiler)
}

// Configurazione obbligatoria del plugin Room 3
room3 {
    // Cartella dove Room salverà i file JSON con lo storico degli schemi
    schemaDirectory("$projectDir/schemas")
}
```

---

## 4. I Due Trabocchetti Frequenti di Gradle

### Trabocchetto 1: Perché `kspJvm` invece di `ksp`?
Nei progetti Android classici monolitici si usa `ksp(libs.androidx.room3.compiler)`. 
Nei progetti **Kotlin Multiplatform**, Kotlin compila target separati (la JVM per il Desktop, il bytecode per Android, il codice macchina per iOS). 

Se scrivi solo `ksp(...)`, Gradle potrebbe non collegare il compilatore alla sorgente JVM, e riceverai l'errore:
```
Unresolved reference: AppDatabase_Impl
```
Usando `add("kspJvm", libs.androidx.room3.compiler)`, indichi esplicitamente a Gradle che durante la compilazione del target desktop JVM deve essere eseguito il generatore di Room 3.

---

### Trabocchetto 2: Perché `schemaDirectory` è obbligatorio?
Quando applichi il plugin `androidx.room3`, Room verifica a ogni build la consistenza delle tue tabelle e genera un file `.json` che descrive esattamente com'è fatto il tuo database. 

Se ometti il blocco:
```kotlin
room3 {
    schemaDirectory("$projectDir/schemas")
}
```
la compilazione fallirà con il messaggio:
```
Schema export directory is not provided to the annotation processor so we cannot export the schema.
```
Questo file JSON è fondamentale per le **migrazioni automatiche** quando rilascerai aggiornamenti della tua applicazione senza voler cancellare i dati degli utenti (approfondiremo questo nel Capitolo 7).

---

## 5. Verifica della Configurazione

Dopo aver aggiornato i file Gradle:

1. **Effettua il Sync di Gradle** in IntelliJ IDEA / Android Studio (icona dell'elefante con la freccia blu in alto a destra).
2. Apri il terminale del progetto ed esegui:
   ```bash
   ./gradlew compileKotlinJvm
   ```
   (Su Windows PowerShell puoi usare `.\gradlew.bat compileKotlinJvm`).

Se il comando termina con **`BUILD SUCCESSFUL`**, il tuo ambiente di sviluppo è pronto per definire le tabelle e i DAO. Nel prossimo capitolo vedremo come modellare le **Entity** e gestire le relazioni tra tabelle.
