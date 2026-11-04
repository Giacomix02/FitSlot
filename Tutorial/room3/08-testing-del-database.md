# Capitolo 8: Testing del Database con Room 3

I test automatici sono un pilastro fondamentale delle metodologie agili e dello sviluppo software di qualità. Testare il database ti permette di verificare che le query SQL funzionino esattamente come previsto, che i vincoli di chiave esterna proteggano i dati e che gli inserimenti restituiscano i valori attesi.

In questo capitolo impariamo a testare i DAO di Room 3 usando un database **In-Memory** (in memoria RAM), rendendo i test incredibilmente veloci, isolati e senza lasciare file temporanei sul disco.

---

## 1. Perché Usare un Database "In-Memory"?

Se ogni test scrivesse sul file fisico `fitslot.db` presente sul disco fisso:
1. I test sarebbero lenti a causa dell'accesso all'SSD/HDD.
2. I test si influenzerebbero a vicenda: un dato inserito dal test A potrebbe far fallire il conteggio del test B.
3. Dovresti ricordarti di ripulire le tabelle dopo ogni test per evitare di sporcare l'ambiente.

La soluzione perfetta offerta da Room e SQLite è l'**In-Memory Database**:
- Il database viene creato **esclusivamente nella memoria RAM**.
- È veloce come un lampo (pochi millisecondi per test).
- Quando il test termina e chiami `db.close()`, il database viene distrutto all'istante, garantendo una tabula rasa per il test successivo.

---

## 2. Configurazione delle Dipendenze di Test

Per testare le funzioni `suspend` e i `Flow` dei nostri DAO, abbiamo bisogno della libreria per il testing delle coroutine (`kotlinx-coroutines-test`) e di `kotlin-test`.

Verifica che in `gradle/libs.versions.toml` o in `shared/build.gradle.kts` sia presente il supporto per i test:

```kotlin
// In shared/build.gradle.kts
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
        }
    }
}
```

---

## 3. Struttura di una Classe di Test per i DAO

I test specifici per la JVM desktop risiedono nella cartella:
`shared/src/jvmTest/kotlin/it/univaq/grr/fitslot/data/local/`

Ecco la struttura completa e commentata di `WorkoutDaoTest`:

```kotlin
package it.univaq.grr.fitslot.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.dao.SlotDao
import it.univaq.grr.fitslot.data.local.dao.WorkoutDao
import it.univaq.grr.fitslot.data.local.entity.SlotEntity
import it.univaq.grr.fitslot.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WorkoutDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var slotDao: SlotDao

    /**
     * Eseguito prima di OGNI singolo test:
     * Crea un database nuovo di zecca nella memoria RAM.
     */
    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
            
        workoutDao = database.workoutDao()
        slotDao = database.slotDao()
    }

    /**
     * Eseguito dopo OGNI singolo test:
     * Chiude la connessione e cancella completamente il database dalla RAM.
     */
    @AfterTest
    fun tearDown() {
        database.close()
    }

    // ==========================================
    // TEST 1: Inserimento e lettura per ID
    // ==========================================
    @Test
    fun testInsertAndGetById() = runTest {
        // 1. Arrange: Prepariamo il dato
        val entity = WorkoutEntity(
            name = "Panca Piana",
            durationMinutes = 45,
            notes = "Focus su tecnica",
            createdAt = 1000L
        )

        // 2. Act: Inseriamo nel database
        val generatedId = workoutDao.insert(entity)

        // 3. Assert: Verifichiamo che il dato letto corrisponda
        val retrieved = workoutDao.getById(generatedId)
        assertNotNull(retrieved, "L'allenamento inserito dovrebbe essere recuperabile!")
        assertEquals("Panca Piana", retrieved.name)
        assertEquals(45, retrieved.durationMinutes)
        assertEquals(generatedId, retrieved.id)
    }

    // ==========================================
    // TEST 2: Aggiornamento con @Upsert
    // ==========================================
    @Test
    fun testUpsertUpdatesExistingRecord() = runTest {
        // Inserimento iniziale
        val id = workoutDao.insert(
            WorkoutEntity(name = "Squat", durationMinutes = 30, createdAt = 1000L)
        )

        // Eseguiamo un upsert con lo stesso ID ma dati modificati
        workoutDao.upsert(
            WorkoutEntity(id = id, name = "Squat Pesante", durationMinutes = 60, createdAt = 1000L)
        )

        val updated = workoutDao.getById(id)
        assertNotNull(updated)
        assertEquals("Squat Pesante", updated.name)
        assertEquals(60, updated.durationMinutes)
    }

    // ==========================================
    // TEST 3: Cancellazione a Cascata (Foreign Key)
    // ==========================================
    @Test
    fun testCascadeDeleteRemovesSlots() = runTest {
        // Inseriamo un workout padre
        val workoutId = workoutDao.insert(
            WorkoutEntity(name = "Circuito", durationMinutes = 60, createdAt = 1000L)
        )

        // Inseriamo uno slot figlio collegato con foreign key
        val slotId = slotDao.insert(
            SlotEntity(workoutId = workoutId, startMinute = 600, capacity = 10)
        )

        // Verifichiamo che lo slot sia presente
        assertNotNull(slotDao.getById(slotId))

        // ORA cancelliamo il padre
        workoutDao.deleteById(workoutId)

        // Assert: Lo slot figlio deve essere sparito automaticamente grazie a CASCADE!
        val orphanSlot = slotDao.getById(slotId)
        assertNull(orphanSlot, "Lo slot figlio doveva essere eliminato a cascata!")
    }

    // ==========================================
    // TEST 4: Verifica del Flow Reattivo
    // ==========================================
    @Test
    fun testObserveAllFlowEmitsUpdatedList() = runTest {
        // All'inizio il Flow deve emettere una lista vuota
        val initialList = workoutDao.observeAll().first()
        assertEquals(0, initialList.size)

        // Inseriamo due elementi
        workoutDao.insert(WorkoutEntity(name = "W1", durationMinutes = 30, createdAt = 1000L))
        workoutDao.insert(WorkoutEntity(name = "W2", durationMinutes = 45, createdAt = 2000L))

        // first() preleverà la prima emissione aggiornata dopo gli inserimenti
        val updatedList = workoutDao.observeAll().first()
        assertEquals(2, updatedList.size)
    }
}
```

---

## 4. Come Eseguire i Test

Puoi eseguire i test in due modi:

### Da Linea di Comando (Terminale)
Esegui il task Gradle specifico per i test JVM del modulo `shared`:

```bash
./gradlew :shared:jvmTest
```
*(Su Windows PowerShell usa: `.\gradlew.bat :shared:jvmTest`)*.

Al termine, Gradle genererà un resoconto HTML dettagliato in:
`shared/build/reports/tests/jvmTest/index.html`

### Dall'IDE (IntelliJ IDEA)
Accanto al nome della classe `class WorkoutDaoTest` o accanto a ciascuna funzione `@Test`, troverai una piccola icona verde con la freccia "Play" (▶️). Cliccala e seleziona **"Run 'WorkoutDaoTest'"** per visualizzare i risultati nella finestra inferiore dei test con spunte verdi o rosse.

---

## 5. Riepilogo del Capitolo

- I test sui database devono essere **veloci, ripetibili e isolati**: usiamo `Room.inMemoryDatabaseBuilder<AppDatabase>()`.
- Inizializza il database in memoria in `@BeforeTest` e ricordati di chiuderlo in `@AfterTest` con `database.close()`.
- Le funzioni sospensive e le coroutine nei test si testano avvolgendo il corpo del test nel builder `runTest { ... }`.
- Per verificare i `Flow` reattivi all'interno di un test unitario, puoi utilizzare l'operatore terminale `.first()`.

Nel prossimo e conclusivo capitolo troverai le **Best Practices Ufficiali** e la **Guida alla Risoluzione di Tutti gli Errori Comuni** in Room 3.
