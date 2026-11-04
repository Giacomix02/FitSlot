# Capitolo 7: Transazioni, Versioning e Migrazioni

Un database non è una struttura statica: l'applicazione cresce nel tempo, vengono richieste nuove funzionalità, si aggiungono campi alle tabelle o si creano nuove relazioni. 

In questo capitolo affrontiamo due argomenti vitali per l'affidabilità di FitSlot:
1. **Le Transazioni:** come garantire che operazioni multiple avvengano secondo il principio "tutto o niente" (ACID).
2. **Le Migrazioni:** come aggiornare la struttura delle tabelle senza far crashare l'applicazione e senza cancellare i dati già salvati dagli utenti.

---

## 1. Cosa sono le Transazioni e Perché Servono

Immagina questa situazione in FitSlot:
- L'utente crea un nuovo Workout ("Crossfit") con 3 Slot orari associati.
- Il programma inserisce il Workout nel database SQLite (operazione riuscita).
- Subito dopo, mentre inserisce il secondo Slot, il computer si spegne improvvisamente o si verifica un errore imprevisto.

Cosa rimane nel database? Un allenamento con dati parziali o corrotti.

Una **Transazione** è un'unità logica di lavoro che segue le proprietà **ACID** (Atomicità, Coerenza, Isolamento, Durabilità).
La regola fondamentale è: **O tutte le operazioni hanno successo, oppure nessuna operazione viene applicata** (se qualcosa fallisce, il database esegue un *rollback* e torna allo stato iniziale).

---

## 2. Come Scrivere Transazioni in Room 3

In Room 3 abbiamo due modalità per gestire le transazioni:

### Modalità A: A livello di DAO con `@Transaction`
È la scelta più elegante e pulita quando le operazioni coinvolgono lo stesso DAO:

```kotlin
@Dao
interface WorkoutDao {
    @Insert suspend fun insertWorkout(workout: WorkoutEntity): Long
    @Insert suspend fun insertSlots(slots: List<SlotEntity>)

    /**
     * Esegue le scritture all'interno di una transazione atomica.
     */
    @Transaction
    suspend fun createWorkoutWithInitialSlots(workout: WorkoutEntity, slots: List<SlotEntity>) {
        val newId = insertWorkout(workout)
        val linkedSlots = slots.map { it.copy(workoutId = newId) }
        insertSlots(linkedSlots)
    }
}
```

---

### Modalità B: A livello di Database (`withWriteTransaction`)

Cosa succede se devi coordinare DAO diversi (ad esempio `WorkoutDao` e `UserDao`)?
In Room 2 esisteva il metodo `database.withTransaction { }`.

> [!IMPORTANT]
> **Novità di Room 3:**
> Per supportare i driver SQLite moderni multipiattaforma e ottimizzare la concorrenza tra thread, Room 3 introduce due funzioni esplicite:
> - **`withWriteTransaction { }`**: per bloccare il database in scrittura ed eseguire modifiche atomiche.
> - **`withReadTransaction { }`**: per letture complesse che richiedono una vista temporale coerente e immutabile dei dati.

Esempio pratico:

```kotlin
import androidx.room3.withWriteTransaction

suspend fun importFullBackup(database: AppDatabase, backupData: BackupData) {
    // Esegue tutto in una singola transazione di scrittura
    database.withWriteTransaction {
        database.workoutDao().insertAll(backupData.workouts)
        database.slotDao().insertAll(backupData.slots)
        // Se un'istruzione qui dentro lancia un'eccezione,
        // TUTTI i dati inseriti vengono immediatamente annullati!
    }
}
```

---

## 3. Il Ciclo di Vita dello Schema: Perché il Database Crasha?

Quando avvii FitSlot per la prima volta con:
```kotlin
@Database(entities = [WorkoutEntity::class], version = 1)
```
Room crea il file `fitslot.db` sul computer e registra al suo interno una tabella speciale che dice: *"Questo database è alla versione 1 e ha queste tabelle"*.

Cosa succede se il giorno dopo modifichi la classe `WorkoutEntity` aggiungendo un nuovo campo (ad esempio `val difficulty: Int`) e riavvii l'applicazione?
All'avvio, l'applicazione andrà immediatamente in crash con questa eccezione:

```
java.lang.IllegalStateException: Room cannot verify the data integrity. 
Looks like you've changed schema but forgot to update the version number. 
You can simply fix this by increasing the version number.
```

Per risolvere il problema devi:
1. Aumentare il numero di `version` in `@Database(version = 2)`.
2. Dire a Room come aggiornare il vecchio file SQLite per farlo combaciare con la nuova versione del codice.

---

## 4. La Cartella `schemas/`: Perché Va Committata su Git

Nel Capitolo 2 abbiamo configurato:
```kotlin
room3 {
    schemaDirectory("$projectDir/schemas")
}
```
Ogni volta che compili con `exportSchema = true`, Room genera un file JSON (es. `1.json`, `2.json`) nella cartella `shared/schemas/`. 

Questo file contiene l'impronta digitale matematica di tutte le tabelle, colonne e indici di quella specifica versione.

> [!WARNING]
> **Regola per il team:** La cartella `schemas/` **deve sempre essere committata su Git**. Non metterla mai nel `.gitignore`! È l'unico modo con cui Room e il compilatore possono confrontare la versione precedente con quella nuova per calcolare le differenze automatiche.

---

## 5. Migrazioni Automatiche (`autoMigrations`)

Per le modifiche più frequenti (aggiungere una colonna, creare una nuova tabella, eliminare una tabella), Room 3 è in grado di scrivere il codice SQL al posto tuo grazie a **`autoMigrations`**:

```kotlin
@Database(
    entities = [WorkoutEntity::class, SlotEntity::class],
    version = 2, // 1. Abbiamo incrementato la versione da 1 a 2
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2) // 2. Room calcola da solo le modifiche confrontando 1.json con 2.json!
    ]
)
abstract class AppDatabase : RoomDatabase() { ... }
```

### Regole per le nuove colonne nelle auto-migrazioni:
Se aggiungi una nuova proprietà non-nulla a una `@Entity`, SQLite pretende un valore predefinito da assegnare alle righe già esistenti nel computer dell'utente:
```kotlin
// In WorkoutEntity:
@ColumnInfo(name = "difficulty", defaultValue = "1")
val difficulty: Int = 1
```

---

## 6. Migrazioni Manuali in Room 3 (`SQLiteConnection`)

Quando la modifica è troppo complessa per le auto-migrazioni (ad esempio: convertire una colonna di testo in numeri, dividere una tabella in due o trasformare i dati), dobbiamo scrivere una migrazione manuale.

> [!IMPORTANT]
> **Come cambia la migrazione manuale in Room 3:**
> In Room 2 si sovrascriveva `migrate(db: SupportSQLiteDatabase)`.
> In **Room 3**, la migrazione è asincrona (`suspend`) e riceve un oggetto **`SQLiteConnection`**!

### Esempio: Aggiunta manuale di una colonna con query DDL

```kotlin
package it.univaq.grr.fitslot.data.local.migration

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // Eseguiamo l'istruzione SQL nativa per alterare la tabella esistente
        connection.execSQL(
            "ALTER TABLE workout ADD COLUMN notes TEXT DEFAULT NULL"
        )
    }
}
```

### Registrazione della migrazione nel Builder:

Nel file in cui costruisci il database (`DatabaseBuilder` o `DatabaseFactory`), aggiungi la migrazione al builder:

```kotlin
fun createRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .addMigrations(MIGRATION_1_2) // Registra la migrazione da 1 a 2
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
```

---

## 7. Strategia Consigliata: Sviluppo vs Produzione

Durante il percorso di sviluppo del progetto universitario o in fase di prototipazione rapida, scrivere una migrazione ogni 5 minuti può rallentare il lavoro.

Ecco la strategia intelligente da seguire:

### Fase 1: Sviluppo e Prototipazione
Se cambi spesso le entity e non hai dati preziosi salvati nel DB:
1. **Opzione rapida:** Cancella semplicemente il file `.db` locale (si trova in `~/.fitslot/fitslot.db`). Al successivo avvio dell'app, Room creerà il database aggiornato da zero.
2. **Opzione fallback:** Puoi aggiungere al builder:
   ```kotlin
   builder.fallbackToDestructiveMigration(dropAllTables = true)
   ```
   *Attenzione:* con questo comando, ogni volta che aumenti la versione, Room cancellerà tutte le tabelle e i dati pregressi senza dare errore.

### Fase 2: Consegna e Produzione
Quando l'applicazione viene distribuita ad altri utenti o presentata per la valutazione:
- Disabilita `fallbackToDestructiveMigration`.
- Scrivi sempre `autoMigrations` o migrazioni manuali per garantire che i dati dell'utente rimangano intatti.

---

## 8. Riepilogo del Capitolo

- Le transazioni garantiscono che un gruppo di modifiche venga applicato in blocco secondo il principio ACID: usa `@Transaction` nei DAO o `withWriteTransaction { }` sul database.
- Modificare un'entità senza incrementare la versione del database fa fallire l'avvio con `IllegalStateException`.
- La cartella `schemas/` è essenziale e deve essere tracciata con Git.
- Per modifiche standard usa le **`autoMigrations = [AutoMigration(from = 1, to = 2)]`**.
- Per modifiche avanzate in Room 3 scrivi una classe `Migration(from, to)` con `override suspend fun migrate(connection: SQLiteConnection)`.

Nel prossimo capitolo scopriremo come scrivere **Test Automatici Veloci e Affidabili** per il database usando SQLite in-memory.
