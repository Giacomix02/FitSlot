# Capitolo 3: Entity, Relazioni e Type Converter

In questo capitolo impariamo a modellare i dati della nostra applicazione. Vedremo come trasformare una `data class` Kotlin in una tabella SQLite, come gestire le chiavi esterne per collegare tabelle diverse, come gestire relazioni (1-a-1, 1-a-molti, molti-a-molti) e come salvare tipi complessi come date e orari.

---

## 1. Cos'è un'Entity?

Un'**Entity** (Entità) in Room è una classe Kotlin che rappresenta **una tabella** del database. Ogni istanza di questa classe corrisponde a **una riga** della tabella, e ogni proprietà corrisponde a **una colonna**.

Per dichiarare un'entità basta annotare una `data class` con `@Entity`:

```kotlin
package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "workout")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
```

### Dettagli fondamentali da notare:

1. **`tableName = "workout"`**: Se non specifichi il nome della tabella, Room userà il nome della classe (`WorkoutEntity`). È buona pratica specificare sempre un nome esplicito, solitamente al singolare e in minuscolo.
2. **`@PrimaryKey(autoGenerate = true)`**: Ogni tabella in un database relazionale **deve** avere una chiave primaria che identifica univocamente ogni riga.
   - Con `autoGenerate = true`, SQLite genera automaticamente un ID incrementale (1, 2, 3...).
   - Impostiamo il valore di default `val id: Long = 0`. Quando crei un nuovo oggetto da inserire, lasci `id = 0`: Room e SQLite capiranno che l'ID deve essere generato da zero.
3. **`@ColumnInfo(name = "...")`**: Permette di dare alla colonna SQL un nome diverso rispetto al campo Kotlin. Nel mondo SQL si usa la convenzione `snake_case` (`duration_minutes`), mentre in Kotlin usiamo `camelCase` (`durationMinutes`).
4. **Nullabilità (`String` vs `String?`)**:
   - `val name: String` genera una colonna `TEXT NOT NULL`. SQLite impedirà di inserire righe senza nome.
   - `val notes: String? = null` genera una colonna `TEXT` che può contenere valori `NULL`.

---

## 2. Proprietà ignorate con `@Ignore`

Se vuoi che una proprietà della tua classe Kotlin esista in memoria ma **non** venga salvata nel database SQLite, usa `@Ignore`:

```kotlin
@Entity(tableName = "workout")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val createdAt: Long,
    
    @Ignore
    val isCurrentlySelected: Boolean = false // Questo campo NON diventa una colonna SQL!
) {
    // Quando usi @Ignore, Room richiede un costruttore secondario o valori di default per tutti i campi ignorati
    constructor(id: Long, name: String, durationMinutes: Int, createdAt: Long) : 
        this(id, name, durationMinutes, createdAt, false)
}
```

> [!TIP]
> Spesso è preferibile evitare `@Ignore` e mantenere l'Entity pura per il database, spostando gli stati della UI (come `isCurrentlySelected`) direttamente nel ViewModel o nei modelli di presentazione.

---

## 3. Indici (`@Index`) e Performance

Immagina un dizionario senza ordine alfabetico: per trovare una parola dovresti leggere ogni singola pagina dall'inizio alla fine (operazione chiamata **Full Table Scan**). Un **Indice** è come l'indice analitico di un libro: dice a SQLite dove si trova un dato senza dover scansionare tutta la tabella.

```kotlin
@Entity(
    tableName = "slot",
    indices = [
        Index(value = ["workout_id"]),             // Velocizza le query WHERE workout_id = ?
        Index(value = ["slot_code"], unique = true) // Garantisce che due slot non abbiano lo stesso codice univoco
    ]
)
data class SlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    @ColumnInfo(name = "slot_code") val slotCode: String,
    @ColumnInfo(name = "start_minute") val startMinute: Int,
    @ColumnInfo(name = "capacity") val capacity: Int
)
```

- **`unique = true`**: Crea un vincolo di unicità: se provi a inserire una riga con un valore duplicato, SQLite solleverà un'eccezione di violazione vincolo.

---

## 4. Chiavi Esterne (`@ForeignKey`) e Integrità Referenziale

In FitSlot, ogni **Slot** (orario di lezione o allenamento) appartiene a un **Workout** genitore.
Cosa succede se cancelliamo un allenamento che ha 10 slot associati?
- Senza chiavi esterne, quegli slot rimarrebbero "orfani" nel database, occupando spazio e creando dati corrotti.
- Con una **Foreign Key**, SQLite garantisce l'integrità dei dati.

```kotlin
@Entity(
    tableName = "slot",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE,     // Se cancelli il Workout genitore, cancella automaticamente tutti i suoi slot!
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workout_id"])] // ⚠️ Room richiede SEMPRE un indice sulla colonna childColumns!
)
data class SlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    @ColumnInfo(name = "start_minute") val startMinute: Int,
    @ColumnInfo(name = "capacity") val capacity: Int
)
```

### Le azioni di `onDelete`:
- **`ForeignKey.CASCADE`**: (Consigliata per relazioni padre-figlio) Cancellando il genitore, vengono eliminati tutti i figli collegati.
- **`ForeignKey.RESTRICT`**: Impedisce la cancellazione del genitore finché esistono figli collegati ad esso.
- **`ForeignKey.SET_NULL`**: Pone a `NULL` la colonna della chiave esterna nel figlio (richiede che la colonna sia nullable).

---

## 5. Come gestire le Relazioni (senza la trappola del Lazy Loading)

In framework pesanti come Hibernate (Java) esiste il cosiddetto "Lazy Loading": quando accedi a `workout.slots`, la libreria esegue segretamente una query sul thread corrente. Questo causa blocchi dell'interfaccia utente e bug insidiosi.

**Room rifiuta il lazy loading:** ogni caricamento deve essere esplicito, sicuro ed eseguito in background.

Per gestire le relazioni in Room si usano classi contenitore con le annotazioni **`@Embedded`** e **`@Relation`**.

> [!IMPORTANT]
> **Le classi di relazione NON sono `@Entity`!** Non rappresentano tabelle fisiche, ma sono semplici classi di aggregazione usate per restituire i risultati di una query.

### Esempio: Relazione 1-a-Molti (Un Workout con tutti i suoi Slot)

```kotlin
package it.univaq.grr.fitslot.data.local.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import it.univaq.grr.fitslot.data.local.entity.SlotEntity
import it.univaq.grr.fitslot.data.local.entity.WorkoutEntity

data class WorkoutWithSlots(
    @Embedded
    val workout: WorkoutEntity,
    
    @Relation(
        parentColumn = "id",         // La colonna chiave in WorkoutEntity
        entityColumn = "workout_id"  // La colonna che fa riferimento al genitore in SlotEntity
    )
    val slots: List<SlotEntity>
)
```

Vedremo nel Capitolo 4 come il DAO utilizzerà questa classe per restituire un allenamento completo dei suoi orari con una singola chiamata reattiva.

---

## 6. Type Converters in Room 3: Gestire Tipi che SQLite non Conosce

SQLite è un motore leggero e supporta nativamente solo **5 tipi primitivi**:
1. `NULL`
2. `INTEGER` (usato per `Int`, `Long`, `Boolean`)
3. `REAL` (usato per `Double`, `Float`)
4. `TEXT` (usato per `String`, `Char`)
5. `BLOB` (usato per array di byte grezzi `ByteArray`)

Cosa succede se vuoi salvare una data (`kotlinx.datetime.LocalDate`, `Instant`) o una lista di stringhe?
Room non sa come scriverle su disco e restituirà l'errore:
```
Cannot figure out how to save this field into database. You can consider adding a type converter for it.
```

In Room 3, per convertire questi tipi si usano le nuove annotazioni:
- **`@ColumnTypeConverter`** (invece del vecchio `@TypeConverter` di Room 2)
- **`@ColumnTypeConverters`** (invece del vecchio `@TypeConverters` di Room 2)

### Creazione della classe Converters

```kotlin
package it.univaq.grr.fitslot.data.local.converter

import androidx.room3.ColumnTypeConverter

class AppTypeConverters {

    // 1. Convertitore per Timestamp in millisecondi (Long <-> Instant)
    // Se usi kotlinx-datetime:
    /*
    @ColumnTypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilliseconds()

    @ColumnTypeConverter
    fun toInstant(epochMilli: Long?): Instant? = epochMilli?.let { Instant.fromEpochMilliseconds(it) }
    */

    // 2. Convertitore per Liste di Stringhe (es. tag: ["Gambe", "Forza"])
    // Salviamo la lista come una singola stringa separata da virgole
    @ColumnTypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(separator = ",")
    }

    @ColumnTypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }
}
```

### Regole ferree per i Type Converter:
1. Per ogni tipo devi scrivere **due funzioni**:
   - Una che trasforma il tuo tipo personalizzato in un tipo SQLite supportato (`String`, `Long`, `Int`, ecc.).
   - Una che compie l'operazione inversa (da tipo SQLite a tipo personalizzato).
2. Entrambe le funzioni devono essere annotate con **`@ColumnTypeConverter`**.
3. Devi registrare la classe dei convertitori nel tuo `@Database` usando `@ColumnTypeConverters(AppTypeConverters::class)`.

> [!TIP]
> **Consiglio per iniziare da neofita:**
> Se puoi evitarlo, non complicarti la vita con troppi converter. Per le date, puoi memorizzare semplicemente un `createdAt: Long` (il timestamp in millisecondi ottenuto con `System.currentTimeMillis()`) direttamente nella tua `@Entity`. Sarà poi il tuo **Mapper** a convertirlo in un formato leggibile per la UI!

---

## 7. Riepilogo del Capitolo

- Una `@Entity` modella una tabella SQLite a partire da una `data class` Kotlin.
- Ogni entità ha bisogno di una `@PrimaryKey(autoGenerate = true)`.
- I campi possono essere rinominati con `@ColumnInfo(name = "...")` per mantenere lo standard `snake_case` su SQLite.
- Usa le `@ForeignKey` con `onDelete = ForeignKey.CASCADE` per collegare tabelle collegate ed evitare dati orfani.
- Le relazioni si modellano tramite data class con `@Embedded` e `@Relation`.
- In Room 3 i tipi complessi si convertono tramite classi annotate con `@ColumnTypeConverter`.

Nel prossimo capitolo analizzeremo il cuore operativo di Room: i **DAO** e la scrittura di query asincrone con `suspend` e `Flow`.
