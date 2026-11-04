# Capitolo 4: DAO, Operazioni CRUD e Query Reattive

Il **DAO (Data Access Object)** è il punto focale in cui definisci tutte le interazioni con il database: inserimenti, aggiornamenti, cancellazioni e query di ricerca. In Room, il DAO è un'**interfaccia Kotlin**: tu dichiari cosa vuoi ottenere, e Room (tramite il compilatore KSP) genera per te tutto il codice SQL e di parsing.

---

## 1. Perché un'Interfaccia?

Nei vecchi modi di programmare, per ogni query dovevi scrivere una classe con decine di righe di codice: aprire la connessione, preparare lo statement, iterare sul cursore, estrarre i dati colonna per colonna e chiudere le risorse.

Con Room, scrivi solo un'interfaccia con annotazioni:

```kotlin
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout")
    fun observeAll(): Flow<List<WorkoutEntity>>
}
```

Durante la compilazione, KSP legge questa firma, controlla che la tabella `workout` esista, controlla che le colonne corrispondano a `WorkoutEntity`, e scrive un file sorgente Kotlin (`WorkoutDao_Impl.kt`) contenente l'implementazione completa e ottimizzata.

---

## 2. La Regola d'Oro di Room 3

> [!CAUTION]
> **In Room 3 le chiamate bloccanti non sono permesse!**
> Se scrivi una funzione normale come `fun getAll(): List<WorkoutEntity>`, il compilatore **rifiuterà di compilare** con un errore.
> 
> In Room 3 le funzioni DAO possono essere solo di **due tipi**:
> 1. Funzioni **`suspend`**: per operazioni una tantum (inserimenti, cancellazioni, letture singole).
> 2. Funzioni che restituiscono **`Flow<T>`**: per letture reattive e continue che si aggiornano da sole.

---

## 3. Operazioni di Scrittura: Create, Update, Delete

Ecco un esempio completo di DAO con tutte le operazioni di scrittura necessarie in **FitSlot**:

```kotlin
package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import it.univaq.grr.fitslot.data.local.entity.WorkoutEntity

@Dao
interface WorkoutDao {

    // ==========================================
    // 1. INSERIMENTO (@Insert)
    // ==========================================

    /**
     * Inserisce un nuovo workout.
     * Restituisce il Long generato come PrimaryKey (l'id assegnato da SQLite).
     */
    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    /**
     * Inserisce una lista di workout. Restituisce la lista dei nuovi ID generati.
     */
    @Insert
    suspend fun insertAll(workouts: List<WorkoutEntity>): List<Long>

    /**
     * Inserimento con strategia di conflitto.
     * Se esiste già una riga con la stessa chiave primaria:
     * - REPLACE: sovrascrive la riga esistente
     * - IGNORE: ignora il nuovo inserimento senza dare errore
     * - ABORT: (default) solleva un'eccezione SQLiteConstraintException
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(workout: WorkoutEntity): Long


    // ==========================================
    // 2. AGGIORNAMENTO (@Update)
    // ==========================================

    /**
     * Cerca la riga con lo stesso 'id' di workout e aggiorna tutte le altre colonne.
     * Restituisce il numero di righe modificate (1 se ha trovato la riga, 0 altrimenti).
     */
    @Update
    suspend fun update(workout: WorkoutEntity): Int


    // ==========================================
    // 3. UPSERT (@Upsert) - La combinazione perfetta
    // ==========================================

    /**
     * Se l'ID non esiste o è 0 -> esegue un INSERT.
     * Se l'ID esiste già nel database -> esegue un UPDATE.
     * Questa annotazione semplifica enormemente la sincronizzazione dati!
     */
    @Upsert
    suspend fun upsert(workout: WorkoutEntity)


    // ==========================================
    // 4. CANCELLAZIONE (@Delete & @Query)
    // ==========================================

    /**
     * Cancella la riga corrispondente all'oggetto passato (match sulla PrimaryKey).
     */
    @Delete
    suspend fun delete(workout: WorkoutEntity): Int

    /**
     * Cancellazione mirata tramite query SQL.
     * È più efficiente se conosci solo l'ID e non vuoi prima fare una SELECT per avere l'oggetto intero.
     */
    @Query("DELETE FROM workout WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Long)

    @Query("DELETE FROM workout")
    suspend fun deleteAll()
}
```

---

## 4. Operazioni di Lettura: `suspend` vs `Flow`

Capire quando usare `suspend fun` e quando usare `Flow` è la competenza più importante per lavorare bene con Room 3:

| Esigenza | Cosa usare nel DAO | Esempio |
|---|---|---|
| Voglio leggere un dato **una sola volta** (ad esempio per fare un controllo o aprire una schermata di dettaglio) | `suspend fun` | `suspend fun getById(id: Long): WorkoutEntity?` |
| Voglio mostrare una lista che **si aggiorna automaticamente** a schermo ogni volta che un dato viene inserito, modificato o rimosso | `Flow<...>` | `fun observeAll(): Flow<List<WorkoutEntity>>` |

---

### Lettura Singola con `suspend`

```kotlin
@Dao
interface WorkoutDao {
    /**
     * Cerca un allenamento per ID.
     * Restituisce WorkoutEntity se trovato, oppure null se non esiste nessuna riga con quell'ID.
     */
    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun getById(id: Long): WorkoutEntity?

    /**
     * Conta quanti workout sono presenti in totale.
     */
    @Query("SELECT COUNT(*) FROM workout")
    suspend fun count(): Int
}
```

> [!NOTE]
> **Come funzionano i parametri `:id`?**
> Il duepunti (`:id`) dice a Room: *"Prendi il valore del parametro Kotlin chiamato `id` e inseriscilo in modo sicuro nella query SQL"*. Room utilizza internamente uno **statement preparato** (`?`), rendendo il codice totalmente immune da attacchi di **SQL Injection**.

---

### Lettura Reattiva con `Flow` (Il vero superpotere di Room)

Un `Flow` è una sequenza asincrona di valori emessi nel tempo. Con Room:

1. Quando chiami `observeAll()`, Room esegue la query SQL iniziale ed emette la prima lista di dati.
2. Room registra un listener interno sul file del database SQLite (meccanismo di **Invalidation Tracker**).
3. Non appena **qualsiasi parte dell'applicazione** esegue un `INSERT`, `UPDATE` o `DELETE` sulla tabella `workout`, Room si accorge del cambiamento, ri-esegue la query in background ed emette automaticamente la nuova lista aggiornata attraverso il `Flow`.
4. L'interfaccia utente (Compose) riceve la nuova lista e ridisegna immediatamente lo schermo!

```kotlin
@Dao
interface WorkoutDao {
    /**
     * Flusso continuo di tutti i workout, ordinati dal più recente.
     * Non è una funzione suspend: restituisce immediatamente il Flow!
     */
    @Query("SELECT * FROM workout ORDER BY created_at DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>
}
```

---

## 5. Query SQL Avanzate

Room ti permette di sfruttare tutta la potenza del dialetto SQLite standard.

### 1. Ricerca di Testo con `LIKE`
```kotlin
@Query("SELECT * FROM workout WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
fun searchByName(query: String): Flow<List<WorkoutEntity>>
```
*(L'operatore `||` in SQLite concatena le stringhe: cercherà qualsiasi nome che contenga la parola passata in `:query`)*.

### 2. Filtro su Lista di Valori con `IN`
```kotlin
@Query("SELECT * FROM workout WHERE id IN (:workoutIds)")
suspend fun getByIds(workoutIds: List<Long>): List<WorkoutEntity>
```
*(Room espande automaticamente la lista di Kotlin `(:workoutIds)` nella sintassi SQL `(?, ?, ?)`).*

### 3. Paginazione Semplice con `LIMIT` e `OFFSET`
```kotlin
@Query("SELECT * FROM workout ORDER BY created_at DESC LIMIT :pageSize OFFSET :pageNumber * :pageSize")
suspend fun getPage(pageSize: Int, pageNumber: Int): List<WorkoutEntity>
```

### 4. Proiezioni (Selezionare solo alcune colonne)
Non sempre hai bisogno di tutte le colonne di una tabella. Puoi definire una `data class` leggera e farla popolare direttamente da Room:

```kotlin
// Data class leggera per un menu a tendina
data class WorkoutDropdownItem(
    val id: Long,
    val name: String
)

@Dao
interface WorkoutDao {
    @Query("SELECT id, name FROM workout ORDER BY name ASC")
    fun observeDropdownItems(): Flow<List<WorkoutDropdownItem>>
}
```

---

## 6. Query di Relazione con `@Transaction`

Nel Capitolo 3 abbiamo creato la classe `WorkoutWithSlots` per rappresentare un allenamento con tutti i suoi orari. Ecco come interrogarla nel DAO:

```kotlin
package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import it.univaq.grr.fitslot.data.local.relation.WorkoutWithSlots
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    /**
     * Restituisce tutti i workout e per ciascuno popola la lista degli slot collegati.
     * ⚠️ L'annotazione @Transaction è OBBLIGATORIA quando si usa @Relation:
     * Room deve eseguire due SELECT separate (una su 'workout' e una su 'slot')
     * e la transazione garantisce che i dati siano coerenti tra le due letture.
     */
    @Transaction
    @Query("SELECT * FROM workout")
    fun observeAllWithSlots(): Flow<List<WorkoutWithSlots>>

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :workoutId")
    suspend fun getWorkoutWithSlots(workoutId: Long): WorkoutWithSlots?
}
```

---

## 7. Metodi Transazionali con Corpo nell'Interfaccia

Le interfacce Kotlin moderne permettono di inserire metodi con una logica predefinita (default methods). Possiamo sfruttare questa caratteristica abbinata a `@Transaction` per eseguire più scritture in modo atomico (tutte o nessuna):

```kotlin
@Dao
interface WorkoutDao {
    @Insert suspend fun insertWorkout(workout: WorkoutEntity): Long
    @Insert suspend fun insertSlots(slots: List<SlotEntity>)

    /**
     * Inserisce un allenamento e contestualmente i suoi slot orari.
     * Se l'inserimento degli slot fallisce, anche l'allenamento viene annullato!
     */
    @Transaction
    suspend fun insertWorkoutWithSlots(workout: WorkoutEntity, slots: List<SlotEntity>) {
        val newWorkoutId = insertWorkout(workout)
        // Aggiorniamo la foreign key degli slot con l'id appena generato
        val slotsWithParentId = slots.map { it.copy(workoutId = newWorkoutId) }
        insertSlots(slotsWithParentId)
    }
}
```

---

## 8. Riepilogo del Capitolo

- I DAO sono **interfacce annotate con `@Dao`**; KSP genera automaticamente il codice SQLite a tempo di compilazione.
- Le scritture avvengono con `@Insert`, `@Update`, `@Upsert` e `@Delete` su funzioni `suspend`.
- In Room 3 **non esistono funzioni bloccanti**: usa `suspend` per letture singole e `Flow` per osservazioni reattive continue.
- Le query con parametri usano la sintassi sicura `:nomeParametro` (protezione anti-SQL injection).
- Usa `@Transaction` ogni volta che una query usa `@Relation` o quando esegui più operazioni di scrittura correlate.

Nel prossimo capitolo vedremo come configurare la classe `@Database` principale e come inizializzare il builder per la nostra applicazione desktop.
