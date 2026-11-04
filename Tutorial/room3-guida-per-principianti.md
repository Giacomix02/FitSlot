# Room 3 spiegato da zero (Kotlin Multiplatform / Desktop)

> 💡 **Nota sulla documentazione completa:** 
> Oltre a questo documento di sintesi rapida, è disponibile l'**intera documentazione approfondita suddivisa in 9 capitoli tematici** nella cartella [`Tutorial/room3/`](room3/) con indice generale in [`Tutorial/README.md`](README.md).
>
> - [Capitolo 1: Fondamenti e Architettura](room3/01-fondamenti-e-architettura.md)
> - [Capitolo 2: Configurazione Gradle e KMP](room3/02-configurazione-gradle-e-kmp.md)
> - [Capitolo 3: Entity, Relazioni e Type Converter](room3/03-entita-e-relazioni.md)
> - [Capitolo 4: DAO, Operazioni CRUD e Query Reattive](room3/04-dao-e-query.md)
> - [Capitolo 5: Database e Inizializzazione Desktop](room3/05-database-e-desktop.md)
> - [Capitolo 6: Clean Architecture, Repository e Integrazione UI](room3/06-architettura-clean-e-ui.md)
> - [Capitolo 7: Transazioni, Versioning e Migrazioni](room3/07-transazioni-e-migrazioni.md)
> - [Capitolo 8: Testing del Database con Room 3](room3/08-testing-del-database.md)
> - [Capitolo 9: Best Practices e Risoluzione Errori Comuni](room3/09-best-practices-e-errori-comuni.md)

Guida pensata per chi inizia con Kotlin. Spiega **come funziona Room 3**, mostra esempi di codice e si ferma sui punti dove i principianti si bloccano più spesso.

> **Stato della versione (novembre 2026):** Room 3.0.0 è uscita il 1 luglio 2026 e la 3.0.1 il 29 luglio 2026. Controlla sempre la pagina delle release per la versione più recente: <https://developer.android.com/jetpack/androidx/releases/room3>
>
> **Attenzione ai tutorial online:** quasi tutti parlano di **Room 2.x** (package `androidx.room`). In Room 3 il package è `androidx.room3` e alcuni nomi sono cambiati (vedi sezione 2). Se copi codice da un tutorial, cambia gli import e confronta con questa guida.

---

## Indice

1. [Cos'è Room e come ragiona](#1-cosè-room-e-come-ragiona)
2. [Cosa cambia in Room 3 rispetto a Room 2](#2-cosa-cambia-in-room-3-rispetto-a-room-2)
3. [Setup Gradle](#3-setup-gradle)
4. [Entity: le tabelle](#4-entity-le-tabelle)
5. [DAO: le query](#5-dao-le-query)
6. [Database e builder (parte desktop)](#6-database-e-builder-parte-desktop)
7. [I punti difficili](#7-i-punti-difficili)
8. [Test del database](#8-test-del-database)
9. [Errori comuni e come risolverli](#9-errori-comuni-e-come-risolverli)
10. [Link utili](#10-link-utili)
11. [Percorso di studio consigliato](#11-percorso-di-studio-consigliato)

---

## 1. Cos'è Room e come ragiona

Le app desktop salvano i dati in un file **SQLite** (un database in un singolo file `.db`). SQLite parla SQL, mentre tu scrivi Kotlin. **Room è lo strato che li collega**: tu descrivi tabelle e query con classi e annotazioni, e Room **genera il codice** che parla con SQLite.

Room ha **tre pezzi**, sempre gli stessi:

```
┌────────────┐      ┌────────────┐      ┌──────────────┐
│  @Entity   │      │   @Dao     │      │  @Database   │
│ una tabella│◄─────│ le query   │◄─────│ il punto di  │
│ (data class)│     │ (interfaccia)│    │ ingresso     │
└────────────┘      └────────────┘      └──────────────┘
```

| Pezzo | Cos'è | Tu scrivi | Room genera |
|---|---|---|---|
| `@Entity` | Una tabella | Una `data class` | Lo schema SQL (`CREATE TABLE`) |
| `@Dao` | Le operazioni sui dati | Un'**interfaccia** con firme e SQL | L'implementazione delle funzioni |
| `@Database` | Il database intero | Una classe astratta che elenca entity e DAO | Il codice che apre il file e crea le tabelle |

### Come funziona la generazione del codice (KSP)

Room usa **KSP** (Kotlin Symbol Processing). Quando compili, KSP legge le tue annotazioni e **scrive nuovi file Kotlin** (di solito in `build/generated/ksp/...`). Per questo:

- se sbagli un'annotazione, l'errore compare **in fase di compilazione**, non a runtime (è un vantaggio);
- le classi generate non le scrivi tu, ma puoi aprirle per curiosità;
- se qualcosa "non si genera", quasi sempre è un problema di configurazione KSP (sezione 3).

Documentazione KSP: <https://kotlinlang.org/docs/ksp-overview.html>

---

## 2. Cosa cambia in Room 3 rispetto a Room 2

Room 3 è un aggiornamento di versione maggiore, focalizzato su **Kotlin Multiplatform**. I concetti restano gli stessi (Entity, DAO, Database), cambiano nomi e regole.

| Argomento | Room 2.x (tutorial vecchi) | Room 3 (quello che usi tu) |
|---|---|---|
| Package | `androidx.room.*` | `androidx.room3.*` |
| Artefatti | `androidx.room:room-runtime` | `androidx.room3:room3-runtime` |
| Plugin Gradle | `androidx.room` + blocco `room { }` | `androidx.room3` + blocco `room3 { }` |
| Generazione codice | KSP **o** KAPT, anche Java | **Solo KSP**, solo codice Kotlin |
| Funzioni DAO | Potevano essere bloccanti | Devono essere `suspend` (oppure restituire `Flow`) |
| Thread | `Executor` | `CoroutineContext` (`setQueryCoroutineContext`) |
| Accesso al DB | `SupportSQLiteDatabase` | `SQLiteDriver` / `SQLiteConnection` |
| Converter di tipo | `@TypeConverter`, `@TypeConverters` | `@ColumnTypeConverter`, `@ColumnTypeConverters` |
| Migrazioni | `migrate(db: SupportSQLiteDatabase)` | `suspend fun migrate(connection: SQLiteConnection)` |
| Transazioni | `withTransaction { }` | `withWriteTransaction { }` / `withReadTransaction { }` |
| Osservare le tabelle | `InvalidationTracker.Observer` | Basato su `Flow` (l'`Observer` è stato rimosso) |
| Target | Android, iOS, JVM, Native | + JS e WasmJS |

Novità utili di Room 3: parametri con valori di default nelle data class dei risultati delle query, tipi di ritorno DAO personalizzati, supporto FTS5 (`@Fts5`), tabelle `WITHOUT ROWID`.

Pagine ufficiali:

- Note di rilascio e novità: <https://developer.android.com/jetpack/androidx/releases/room3>
- Guida alla migrazione da Room 2 (utile anche come "elenco delle differenze"): <https://developer.android.com/training/data-storage/room/migration-2-to-3>

> **Buona notizia:** la documentazione ufficiale dice che gran parte della guida generale "Salvare dati in un database locale con Room" vale ancora per Room 3. Quindi le pagine su Entity, DAO e relazioni sono valide, basta usare i nuovi import.

---

## 3. Setup Gradle

> Nomi e versioni vanno **sempre verificati** sulla documentazione, perché cambiano spesso. Qui trovi la struttura generale.

### 3.1 `gradle/libs.versions.toml`

```toml
[versions]
room3 = "3.0.1"
sqlite = "2.6.2"          # verifica l'ultima versione di androidx.sqlite
ksp = "<versione compatibile con il tuo Kotlin>"

[libraries]
androidx-room3-runtime  = { module = "androidx.room3:room3-runtime",  version.ref = "room3" }
androidx-room3-compiler = { module = "androidx.room3:room3-compiler", version.ref = "room3" }
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }

[plugins]
ksp           = { id = "com.google.devtools.ksp", version.ref = "ksp" }
androidx-room3 = { id = "androidx.room3", version.ref = "room3" }
```

**Importante:** la versione di KSP deve essere compatibile con la versione di Kotlin che usi. È la causa più comune di errori di build all'inizio.

### 3.2 `shared/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
}

kotlin {
    jvm()   // il tuo target desktop

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

dependencies {
    // Il compilatore Room va aggiunto per OGNI target su cui gira KSP.
    // Per il solo desktop è questo:
    add("kspJvm", libs.androidx.room3.compiler)
    // Se aggiungi Android: add("kspAndroid", ...)
}

room3 {
    // OBBLIGATORIO se usi il plugin Room: dove salvare gli schemi JSON
    schemaDirectory("$projectDir/schemas")
}
```

Cosa fa ogni pezzo:

| Elemento | Perché serve |
|---|---|
| `room3-runtime` | Le classi di Room che usi nel codice (`RoomDatabase`, `@Entity`...) |
| `room3-compiler` + `ksp` | Il generatore di codice |
| `sqlite-bundled` | Un SQLite incluso nell'app: stessa versione e stesso comportamento su tutti i PC |
| `room3 { schemaDirectory }` | Esporta lo schema a ogni cambio di versione (serve per le migrazioni) |

Guida ufficiale per KMP (con gli snippet aggiornati): <https://developer.android.com/kotlin/multiplatform/room>

---

## 4. Entity: le tabelle

Una `data class` annotata con `@Entity` diventa una tabella. Ogni proprietà diventa una colonna.

```kotlin
@Entity(tableName = "workout")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val notes: String? = null,          // String? → colonna che può essere NULL
    val createdAt: Long                 // timestamp in millisecondi
)
```

### Regole fondamentali

- **Serve sempre una chiave primaria** (`@PrimaryKey`).
- Con `autoGenerate = true` il database assegna l'`id`. Metti `id = 0` come default: Room capisce "dammi tu un id nuovo".
- Tipo **nullable** (`String?`) = colonna che accetta NULL. Tipo non nullable = `NOT NULL`.
- Tipi supportati senza fare nulla: `Int`, `Long`, `Double`, `Float`, `Boolean`, `String`, `ByteArray` (e gli enum, di solito gestiti da Room). Altri tipi (date, liste, oggetti) richiedono una conversione (sezione 7.4).

### Annotazioni utili

```kotlin
@Entity(
    tableName = "slot",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE      // cancello il workout → cancello i suoi slot
        )
    ],
    indices = [Index("workoutId")]             // velocizza le ricerche per workoutId
)
data class SlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,                       // la chiave esterna
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,   // nome colonna personalizzato
    val startMinute: Int,                      // minuti dalla mezzanotte
    val capacity: Int
)
```

| Annotazione | Significato |
|---|---|
| `@ColumnInfo(name = "...")` | Nome della colonna diverso da quello della proprietà |
| `@Ignore` | Questa proprietà **non** diventa una colonna |
| `@Index` | Indice per velocizzare le query su quella colonna |
| `@ForeignKey` | Collega due tabelle e definisce cosa fare alla cancellazione |
| `@Embedded` | Incorpora un oggetto dentro la stessa tabella (vedi sezione 7.3) |

Doc: <https://developer.android.com/training/data-storage/room/defining-data>

---

## 5. DAO: le query

Il DAO è un'**interfaccia**: scrivi le firme, Room scrive il corpo.

```kotlin
@Dao
interface WorkoutDao {

    // --- SCRITTURA ---
    @Insert
    suspend fun insert(workout: WorkoutEntity): Long     // restituisce l'id generato

    @Update
    suspend fun update(workout: WorkoutEntity)           // cerca la riga con lo stesso id

    @Upsert
    suspend fun upsert(workout: WorkoutEntity)           // insert se non esiste, update se esiste

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Query("DELETE FROM workout WHERE id = :id")
    suspend fun deleteById(id: Long)

    // --- LETTURA UNA TANTUM (suspend) ---
    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun getById(id: Long): WorkoutEntity?        // null se non esiste

    // --- LETTURA CONTINUA (Flow) ---
    @Query("SELECT * FROM workout ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout WHERE name LIKE '%' || :text || '%'")
    fun search(text: String): Flow<List<WorkoutEntity>>
}
```

### Come leggere le query

- `:id` dentro l'SQL è il **parametro** della funzione con lo stesso nome. Non concatenare mai stringhe a mano: usa sempre i parametri `:nome`.
- Il tipo di ritorno deve combaciare con le colonne selezionate. `SELECT *` → `WorkoutEntity`. Puoi anche restituire un sottoinsieme di colonne con una data class apposta:

```kotlin
data class WorkoutName(val id: Long, val name: String)

@Query("SELECT id, name FROM workout")
fun observeNames(): Flow<List<WorkoutName>>
```

### La regola più importante di Room 3

> **Una funzione DAO o è `suspend`, oppure restituisce un `Flow`.** Una funzione normale (bloccante) che restituisce una lista **non compila**.

Se ti serve l'SQL, il riferimento del dialetto è qui: <https://www.sqlite.org/lang.html>

Doc ufficiale DAO: <https://developer.android.com/training/data-storage/room/accessing-data>

---

## 6. Database e builder (parte desktop)

### 6.1 La classe `AppDatabase` (in `commonMain`)

```kotlin
@Database(
    entities = [WorkoutEntity::class, SlotEntity::class],
    version = 1,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)               // solo se hai dei converter
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun slotDao(): SlotDao
}

// Room genera l'implementazione "actual" di questo oggetto.
// Senza il @Suppress l'IDE mostra un avviso rosso fuorviante.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

**Perché `@ConstructedBy`?** Nel multipiattaforma non si può usare la reflection come su Android. Room genera lui il costruttore per ogni piattaforma, e tu lo dichiari con un `expect object`. Non devi scrivere altro: se dopo la compilazione l'IDE segnala ancora l'errore *"Expected object has no actual declaration"*, controlla il `@Suppress` e che KSP sia configurato.

### 6.2 Il builder specifico del desktop (in `jvmMain`)

Dove si trova il file `.db` dipende dal sistema operativo, quindi questa è l'unica parte che va in `jvmMain`:

```kotlin
// shared/src/jvmMain/kotlin/it/univaq/grr/fitslot/data/local/DatabaseBuilder.kt
fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dir = File(System.getProperty("user.home"), ".fitslot").apply { mkdirs() }
    val dbFile = File(dir, "fitslot.db")
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}
```

> Gli esempi ufficiali usano `java.io.tmpdir` perché sono dimostrativi: la cartella temporanea può essere cancellata dal sistema. Per un'app vera usa la cartella dell'utente (come sopra) o quella dati del sistema operativo.

### 6.3 Costruire il database (in `commonMain`)

```kotlin
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())            // import: androidx.sqlite.driver.bundled.BundledSQLiteDriver
        .setQueryCoroutineContext(Dispatchers.IO)    // le query girano su thread di I/O
        .build()
```

### 6.4 Crea il database **una volta sola**

L'`AppDatabase` è pesante e deve essere un **singleton**: crealo all'avvio (o con Koin come `single { }`) e passalo ai repository. Non chiamare `.build()` ogni volta che ti serve.

```kotlin
// all'avvio dell'app, in un unico punto
val database = getRoomDatabase(getDatabaseBuilder())
val workoutRepository: WorkoutRepository = WorkoutRepositoryImpl(database.workoutDao())
```

Doc: <https://developer.android.com/kotlin/multiplatform/room>

---

## 7. I punti difficili

### 7.1 `suspend` e `Flow`: cosa sono e quando usarli

Sono i due concetti che spaventano di più all'inizio. Regola pratica:

| Voglio... | Uso | Esempio |
|---|---|---|
| Fare **un'azione** (inserire, cancellare, leggere una volta) | `suspend fun` | `suspend fun insert(...)` |
| Avere dati che **si aggiornano da soli** quando la tabella cambia | `Flow<...>` | `fun observeAll(): Flow<List<...>>` |

- Una **`suspend fun`** può "aspettare" senza bloccare l'interfaccia. Si può chiamare solo da un'altra `suspend fun` o da una **coroutine** (`scope.launch { ... }`).
- Un **`Flow`** è un flusso di valori nel tempo. Con Room, ogni volta che la tabella cambia il Flow **emette di nuovo** la lista aggiornata. Questo è ciò che permette alla UI di aggiornarsi da sola quando inserisci un record.

Esempio di uso in un ViewModel (il tipo di ViewModel dipende dalla libreria che usi: ViewModel multipiattaforma di AndroidX, `ScreenModel` di Voyager, ecc.):

```kotlin
class WorkoutListViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    // Trasformo il Flow del repository in uno StateFlow che la UI può osservare
    val workouts: StateFlow<List<Workout>> = repository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addWorkout(name: String, minutes: Int) {
        viewModelScope.launch {                 // le funzioni suspend vanno chiamate in una coroutine
            repository.add(Workout(name = name, duration = minutes))
        }
    }
}
```

E nella UI Compose:

```kotlin
@Composable
fun WorkoutListScreen(viewModel: WorkoutListViewModel) {
    val workouts by viewModel.workouts.collectAsState()

    LazyColumn {
        items(workouts, key = { it.id }) { workout ->
            Text(workout.name)
        }
    }
}
```

Inserisci un workout → Room aggiorna la tabella → il `Flow` emette → la lista si ridisegna. Non devi "ricaricare" nulla a mano.

Per approfondire: <https://kotlinlang.org/docs/coroutines-guide.html> e <https://kotlinlang.org/docs/flow.html>

Doc Room sulle query asincrone: <https://developer.android.com/training/data-storage/room/async-queries>

### 7.2 Entity ≠ modello di dominio (e i mapper)

Come visto nella struttura del progetto, la UI non deve conoscere le `Entity`. Si converte con funzioni di estensione:

```kotlin
// data/mapper/WorkoutMapper.kt
fun WorkoutEntity.toDomain() = Workout(
    id = id,
    name = name,
    duration = durationMinutes,
    notes = notes
)

fun Workout.toEntity(createdAt: Long) = WorkoutEntity(
    id = id,
    name = name,
    durationMinutes = duration,
    notes = notes,
    createdAt = createdAt
)

// e per le liste:
fun List<WorkoutEntity>.toDomain() = map { it.toDomain() }
```

Nel repository: `dao.observeAll().map { it.toDomain() }`.

### 7.3 Relazioni tra tabelle

Room **non carica automaticamente** gli oggetti collegati (non è Hibernate/JPA). Se vuoi un workout con i suoi slot, devi descrivere la relazione con una classe apposta:

```kotlin
// Un workout con la lista dei suoi slot (relazione uno-a-molti)
data class WorkoutWithSlots(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "id",          // colonna in WorkoutEntity
        entityColumn = "workoutId"    // colonna in SlotEntity
    )
    val slots: List<SlotEntity>
)
```

E nel DAO:

```kotlin
@Transaction
@Query("SELECT * FROM workout")
fun observeWithSlots(): Flow<List<WorkoutWithSlots>>
```

- `@Embedded` = "metti qui dentro tutte le colonne di questa entity".
- `@Relation` = "per ogni riga padre, cerca le righe figlie collegate".
- `@Transaction` serve perché Room esegue **più query** (una per i workout, una per gli slot) e vuole che i dati siano coerenti.
- Per relazioni **molti-a-molti** serve una tabella ponte e `@Junction`.

Doc con tutti i casi (uno-a-uno, uno-a-molti, molti-a-molti, annidate):
<https://developer.android.com/training/data-storage/room/relationships>

### 7.4 Tipi che SQLite non conosce (ColumnTypeConverter)

SQLite conosce solo numeri, testo e blob. Se nell'entity hai, per esempio, una `LocalDate`, devi dire a Room come trasformarla in qualcosa di salvabile.

> **Suggerimento per iniziare:** il modo più semplice è **non usare converter**. Salva date come `Long` (millisecondi) o `String` nell'entity e convertile nel **mapper**. Introduci i converter solo quando ti servono davvero.

Se vuoi usarli (nota i nomi di Room 3):

```kotlin
class Converters {
    @ColumnTypeConverter
    fun fromLocalDate(value: LocalDate): String = value.toString()      // es. "2026-11-04"

    @ColumnTypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)
}

@Database(entities = [...], version = 1)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { ... }
```

(`LocalDate` qui è quella di `kotlinx-datetime`, che è la scelta adatta al codice multipiattaforma.)

Regola: per ogni tipo servono **due** funzioni, una in andata e una in ritorno, con firme coerenti.

Doc: <https://developer.android.com/training/data-storage/room/referencing-data>

### 7.5 Versioni, schema e migrazioni

Quando **cambi la struttura di una tabella** (aggiungi una colonna, una tabella...) il file `.db` già creato sul PC dell'utente non combacia più con il codice. Devi:

1. **Aumentare `version`** nell'`@Database` (da 1 a 2).
2. Dire a Room **come passare** dalla versione vecchia alla nuova.

**Durante lo sviluppo** (nessun dato da conservare) la strada più semplice è **cancellare il file `.db`** e lasciare che Room lo ricrei da capo.

**Quando l'app è in uso** servono le migrazioni. Due modi:

**a) Migrazione automatica** (per modifiche semplici: nuova colonna, nuova tabella):

```kotlin
@Database(
    entities = [WorkoutEntity::class, SlotEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class AppDatabase : RoomDatabase() { ... }
```

Funziona solo se gli **schemi JSON** delle versioni 1 e 2 sono presenti nella cartella `schemas/`. Per questo `exportSchema = true` e `schemaDirectory` sono importanti, e la cartella `schemas/` va committata su Git.

**b) Migrazione manuale** (per modifiche complesse). In Room 3 usa `SQLiteConnection` ed è `suspend`:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL(     // import: androidx.sqlite.async.executeSQL
            "ALTER TABLE workout ADD COLUMN difficulty INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val database = getDatabaseBuilder()
    .addMigrations(MIGRATION_1_2)
    .setDriver(BundledSQLiteDriver())
    .build()
```

Nota: nelle `ALTER TABLE ... ADD COLUMN ... NOT NULL` serve un `DEFAULT`, altrimenti SQLite rifiuta l'operazione.

Doc: <https://developer.android.com/training/data-storage/room/migrating-db-versions>

### 7.6 Transazioni: più operazioni "tutte o nessuna"

Se devi fare più scritture che devono riuscire **insieme** (es. creare un workout **e** i suoi slot), usa una transazione: se una fallisce, vengono annullate tutte.

**Opzione semplice, dentro il DAO:**

```kotlin
@Dao
interface WorkoutDao {
    @Insert suspend fun insertWorkout(workout: WorkoutEntity): Long
    @Insert suspend fun insertSlots(slots: List<SlotEntity>)

    @Transaction
    suspend fun insertWorkoutWithSlots(workout: WorkoutEntity, slots: List<SlotEntity>) {
        val id = insertWorkout(workout)
        insertSlots(slots.map { it.copy(workoutId = id) })
    }
}
```

**Opzione generale, dal database** (Room 3 offre `withWriteTransaction` e `withReadTransaction`, che sostituiscono il vecchio `withTransaction`):

```kotlin
database.withWriteTransaction {
    workoutDao.insert(workout)
    slotDao.insertAll(slots)
}
```

(Controlla l'import esatto nell'IDE: sta nel package `androidx.room3`.)

### 7.7 Dove gira il codice: i thread

In Room 3 le query girano su un `CoroutineContext` che scegli tu col builder (`setQueryCoroutineContext(Dispatchers.IO)`). Se non lo imposti, Room usa `Dispatchers.IO` di default. Significa che **non devi** cambiare thread a mano per chiamare il DAO: puoi chiamarlo dal ViewModel senza bloccare la UI.

---

## 8. Test del database

I test del database vanno in `jvmTest` (o `commonTest` se non usano API JVM). Si usa un database **in memoria**: è veloce e sparisce a fine test.

```kotlin
class WorkoutDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WorkoutDao

    @BeforeTest
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        dao = db.workoutDao()
    }

    @AfterTest
    fun tearDown() = db.close()

    @Test
    fun insertAndRead() = runTest {          // runTest viene da kotlinx-coroutines-test
        val id = dao.insert(
            WorkoutEntity(name = "Squat", durationMinutes = 45, createdAt = 0L)
        )

        val loaded = dao.getById(id)

        assertEquals("Squat", loaded?.name)
    }
}
```

Servono le dipendenze di test (`kotlin-test`, `kotlinx-coroutines-test`). Verifica nella documentazione la firma attuale di `inMemoryDatabaseBuilder` per KMP.

Doc: <https://developer.android.com/training/data-storage/room/testing-db>

---

## 9. Errori comuni e come risolverli

| Sintomo | Causa probabile | Soluzione |
|---|---|---|
| `Expected object 'AppDatabaseConstructor' has no actual declaration` | Avviso fuorviante dell'IDE | Aggiungi `@Suppress("KotlinNoActualForExpect")` e controlla che KSP giri |
| La classe generata non esiste / `Unresolved reference` su `AppDatabase_Impl` | KSP non configurato per il target | Controlla `add("kspJvm", ...)` e che il plugin `ksp` sia applicato |
| Errore di build che nomina KSP e Kotlin | Versione di KSP non compatibile con Kotlin | Allinea le due versioni |
| `Cannot figure out how to save this field into database` | Tipo senza converter | Usa tipi base, o scrivi un `@ColumnTypeConverter` |
| `Query method parameters should either be a type that can be converted...` o errori sui DAO | Funzione DAO non `suspend` né `Flow` | Rendila `suspend` oppure fai restituire un `Flow` |
| `Schema export directory is not provided` | Manca `schemaDirectory` | Aggiungi il blocco `room3 { schemaDirectory(...) }` |
| `Room cannot verify the data integrity` / crash all'avvio dopo una modifica | Cambiato lo schema senza cambiare `version` | Aumenta `version` e scrivi la migrazione (in sviluppo cancella il `.db`) |
| `no such table` / `no such column` | Entity non elencata in `@Database(entities = [...])` o query con nome sbagliato | Controlla l'elenco entity e i nomi di tabella/colonna |
| Import `androidx.room.*` che non si risolve | Copiato da un tutorial Room 2 | Sostituisci con `androidx.room3.*` |
| `@TypeConverter` non trovato | Nome di Room 2 | Usa `@ColumnTypeConverter` |
| I dati spariscono a ogni avvio | Database in memoria o in cartella temporanea | Usa un percorso stabile (`user.home`) |
| La UI non si aggiorna dopo un insert | La query restituisce un valore singolo, non un `Flow` | Usa `Flow<...>` per ciò che la UI deve osservare |

Consiglio generale: **leggi sempre il primo errore** del log di build, non l'ultimo. Gli errori di Room/KSP sono in genere molto espliciti e indicano la riga di codice.

---

## 10. Link utili

**Room**

- Guida generale a Room: <https://developer.android.com/training/data-storage/room>
- Entity: <https://developer.android.com/training/data-storage/room/defining-data>
- DAO: <https://developer.android.com/training/data-storage/room/accessing-data>
- Query asincrone: <https://developer.android.com/training/data-storage/room/async-queries>
- Relazioni: <https://developer.android.com/training/data-storage/room/relationships>
- Tipi complessi e converter: <https://developer.android.com/training/data-storage/room/referencing-data>
- Migrazioni: <https://developer.android.com/training/data-storage/room/migrating-db-versions>
- Test: <https://developer.android.com/training/data-storage/room/testing-db>
- Precompilare il database con dati iniziali: <https://developer.android.com/training/data-storage/room/prepopulate>
- Viste (`@DatabaseView`): <https://developer.android.com/training/data-storage/room/creating-views>

**Room 3 e Kotlin Multiplatform**

- Release e novità di Room 3: <https://developer.android.com/jetpack/androidx/releases/room3>
- Setup di Room per KMP: <https://developer.android.com/kotlin/multiplatform/room>
- Migrazione da Room 2 a Room 3: <https://developer.android.com/training/data-storage/room/migration-2-to-3>
- Kotlin Multiplatform su Android Developers: <https://developer.android.com/kotlin/multiplatform>

**Kotlin**

- Coroutine: <https://kotlinlang.org/docs/coroutines-guide.html>
- Flow: <https://kotlinlang.org/docs/flow.html>
- KSP: <https://kotlinlang.org/docs/ksp-overview.html>

**SQL**

- Riferimento SQLite: <https://www.sqlite.org/lang.html>

---

## 11. Percorso di studio consigliato

Per non perderti, procedi per piccoli passi, verificando che compili a ogni passo:

1. **Una sola tabella.** Crea `WorkoutEntity`, `WorkoutDao`, `AppDatabase` e il builder in `jvmMain`. Fai solo `insert` e `observeAll`.
2. **Fai vedere i dati.** Collega il `Flow` a una `LazyColumn` tramite repository e ViewModel. Vedere la lista che si aggiorna da sola è il momento in cui Room "scatta".
3. **Aggiungi i mapper** e il modello di dominio.
4. **Aggiungi una seconda tabella** con chiave esterna e prova `@Relation`.
5. **Scrivi un test** del DAO con database in memoria.
6. **Solo alla fine** guarda migrazioni, converter e transazioni, quando ne hai davvero bisogno.

Se un passo non compila, torna alla sezione 9: quasi tutti i problemi dei primi giorni sono lì.
