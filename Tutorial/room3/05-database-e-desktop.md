# Capitolo 5: Database e Inizializzazione per Desktop

In questo capitolo assembliamo tutti i pezzi visti finora. Creeremo la classe astratta **`AppDatabase`** (il punto di ingresso centrale di Room 3) e vedremo come configurare il builder specifico per l'applicazione **Desktop (JVM)**, spiegando in dettaglio il meccanismo multiplatform di `@ConstructedBy`.

---

## 1. La Classe `AppDatabase` (in `commonMain`)

La classe del database funge da collante tra le tue **Entity** e i tuoi **DAO**. È una classe astratta che estende `RoomDatabase`:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/data/local/AppDatabase.kt
package it.univaq.grr.fitslot.data.local

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import it.univaq.grr.fitslot.data.local.converter.AppTypeConverters
import it.univaq.grr.fitslot.data.local.dao.SlotDao
import it.univaq.grr.fitslot.data.local.dao.WorkoutDao
import it.univaq.grr.fitslot.data.local.entity.SlotEntity
import it.univaq.grr.fitslot.data.local.entity.WorkoutEntity

@Database(
    entities = [
        WorkoutEntity::class,
        SlotEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ColumnTypeConverters(AppTypeConverters::class) // Includi solo se hai creato dei converter
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    // Metodi astratti per ottenere i DAO (Room genererà l'implementazione)
    abstract fun workoutDao(): WorkoutDao
    abstract fun slotDao(): SlotDao
}

// Dichiarazione "expect" per Kotlin Multiplatform
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

---

## 2. Il Meccanismo `@ConstructedBy` Spiegato ai Neofiti

Se vieni da vecchi tutorial di Android o Room 2, questo blocco potrebbe sembrarti strano:
```kotlin
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase()

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

### Perché serve?
Nel vecchio Android (Room 2 su sola JVM/Android), quando chiamavi `Room.databaseBuilder()`, Room cercava a runtime la classe generata `AppDatabase_Impl` usando la **Java Reflection** (`Class.forName(...)`).

Tuttavia, Kotlin Multiplatform è progettato per girare anche dove **la reflection non esiste o è disabilitata** (ad esempio su iOS con codice nativo o sul Web con WebAssembly). 

Per risolvere questo problema, Room 3 adotta un approccio a tempo di compilazione:
1. Con `@ConstructedBy(AppDatabaseConstructor::class)` diciamo a Room: *"Usa questo oggetto per istanziare il database"*.
2. Con `expect object AppDatabaseConstructor` diciamo al compilatore Kotlin: *"Ci sarà un oggetto chiamato AppDatabaseConstructor per ciascuna piattaforma"*.
3. Durante la compilazione, **KSP genera automaticamente il blocco `actual object AppDatabaseConstructor`** per la JVM desktop!

### Perché serve `@Suppress("KotlinNoActualForExpect")`?
Prima che tu prema il pulsante "Run" o compili il progetto, IntelliJ IDEA analizza il codice sorgente. Poiché il file `actual` non è stato ancora generato da KSP, l'IDE potrebbe mostrare una linea rossa con l'avviso:
> *"Expected object 'AppDatabaseConstructor' has no actual declaration in module..."*

Questo avviso è un falso positivo temporaneo. L'annotazione `@Suppress("KotlinNoActualForExpect")` zittisce l'avviso nell'editor, permettendo a Gradle di compilare senza allarmi ingiustificati.

---

## 3. Il Builder Desktop (in `jvmMain`)

Mentre la struttura delle tabelle è identica su qualsiasi piattaforma (quindi sta in `commonMain`), **il percorso fisico in cui salvare il file `.db` dipende dal sistema operativo**.

Sul computer dell'utente (Windows, macOS o Linux), dove dobbiamo salvare il database?

> [!WARNING]
> **Non usare mai `java.io.tmpdir` per un'app reale!**
> La cartella temporanea del sistema operativo viene periodicamente ripulita da Windows o macOS. Se salvi il file lì, i dati dell'utente verranno cancellati a sorpresa al riavvio del PC!

La prassi migliore per un'applicazione desktop è salvare il database all'interno della cartella dell'utente (`user.home`):

```kotlin
// Percorso: shared/src/jvmMain/kotlin/it/univaq/grr/fitslot/data/local/DatabaseBuilder.jvm.kt
package it.univaq.grr.fitslot.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // 1. Identifica la home dell'utente (es: C:\Users\Mario su Windows o /home/mario su Linux)
    val userHome = System.getProperty("user.home")
    
    // 2. Crea una cartella nascosta dedicata all'applicazione
    val appDataDir = File(userHome, ".fitslot").apply {
        if (!exists()) {
            mkdirs() // Crea la cartella se non esiste ancora
        }
    }
    
    // 3. Definisci il file del database SQLite
    val dbFile = File(appDataDir, "fitslot.db")
    
    // 4. Restituisci il builder di Room configurato con il percorso assoluto del file
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}
```

---

## 4. Costruzione del Database (in `commonMain`)

Ora che abbiamo il builder con il percorso del file, possiamo completare la costruzione del database configurando il driver SQLite e i thread di esecuzione:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/data/local/DatabaseFactory.kt
package it.univaq.grr.fitslot.data.local

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun createRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        // 1. Driver SQLite bundled: include il motore SQLite C precompilato
        .setDriver(BundledSQLiteDriver())
        
        // 2. Thread pool per le query: esegue le letture e scritture su thread di I/O,
        // evitando assolutamente di bloccare l'interfaccia grafica Compose!
        .setQueryCoroutineContext(Dispatchers.IO)
        
        // 3. Costruisce l'istanza finale
        .build()
}
```

---

## 5. La Regola d'Oro del Singleton (Crea il DB UNA SOLA VOLTA)

> [!CAUTION]
> **Non chiamare mai `createRoomDatabase()` all'interno di una schermata Compose o di un ViewModel!**

L'oggetto `AppDatabase` è pesante: apre connessioni fisiche al file `.db`, avvia thread interni di monitoraggio per i `Flow` e gestisce la cache di memoria.

Se crei una nuova istanza ogni volta:
- Esaurirai rapidamente la memoria e i thread del sistema operativo.
- SQLite solleverà eccezioni di **file lock** (database locked).
- La reattività dei `Flow` smetterà di funzionare (un'istanza scrive nel file ma l'altra istanza non viene notificata).

### Soluzione semplice (Singleton manuale per FitSlot)

Puoi gestire l'istanza centralmente tramite un oggetto singleton o inizializzarla all'avvio dell'applicazione desktop:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/data/local/DatabaseProvider.kt
package it.univaq.grr.fitslot.data.local

object DatabaseProvider {
    private var instance: AppDatabase? = null

    fun getDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: createRoomDatabase(builder).also { instance = it }
        }
    }
}
```

All'avvio della tua app desktop (in `desktopApp/.../main.kt`), ti basterà fare:

```kotlin
fun main() {
    // 1. Inizializza il database una sola volta all'avvio
    val builder = getDatabaseBuilder()
    val database = DatabaseProvider.getDatabase(builder)
    
    // 2. Estrai i DAO da passare ai tuoi repository
    val workoutDao = database.workoutDao()
    val slotDao = database.slotDao()

    // 3. Avvia la finestra Compose Desktop
    application {
        Window(onCloseRequest = ::exitApplication, title = "FitSlot") {
            App(workoutDao = workoutDao, slotDao = slotDao)
        }
    }
}
```

---

## 6. Riepilogo del Capitolo

- `AppDatabase` è una classe astratta che eredita da `RoomDatabase`, dichiara le tabelle in `@Database(entities = [...])` ed espone i metodi astratti dei DAO.
- In Kotlin Multiplatform usiamo `@ConstructedBy` ed `expect object AppDatabaseConstructor`: KSP genererà l'implementazione concreta durante la compilazione senza usare la reflection a runtime.
- Su Desktop (JVM) usiamo `System.getProperty("user.home")` per posizionare il file `.db` in una cartella protetta e persistente (es. `~/.fitslot/fitslot.db`).
- Usiamo sempre `.setDriver(BundledSQLiteDriver())` e `.setQueryCoroutineContext(Dispatchers.IO)`.
- Il database **deve essere un Singleton**: istanzialo una sola volta all'avvio e condividilo tra i repository.

Nel prossimo capitolo vedremo come collegare il Database alla UI seguendo la **Clean Architecture** (Mapper, Repository, ViewModel e Compose).
