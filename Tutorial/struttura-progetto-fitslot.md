# Guida alla struttura del progetto FitSlot

Guida per capire **cosa mettere in ogni cartella** di un progetto Kotlin Multiplatform (Compose Desktop) con **Room 3** come ORM.

> **Nota su Room 3:** è una versione molto recente e package/artefatti possono differire da Room 2.x. Negli esempi qui sotto gli `import` sono omessi di proposito: usa l'autocompletamento di IntelliJ e controlla la documentazione ufficiale di Android Developers per dipendenze e plugin Gradle.

---

## 1. L'idea di fondo: architettura a strati

Il codice è diviso in tre strati, con una regola semplice sulle dipendenze:

```
   ui  ───────►  domain  ◄───────  data
(cosa vede       (regole e         (come si salvano
 l'utente)        modelli puri)     i dati: Room)
```

- **`ui`** conosce solo `domain`.
- **`data`** conosce `domain` (ne implementa le interfacce).
- **`domain`** non conosce nessuno: è Kotlin puro, senza Room e senza Compose.

Vantaggio: se domani cambi database o interfaccia grafica, tocchi solo uno strato.

---

## 2. Struttura generale del progetto

```
FitSlot/
├── desktopApp/        # Punto d'ingresso dell'app desktop (main + finestra)
├── shared/            # Tutto il codice vero dell'app (logica + UI + DB)
├── gradle/            # Wrapper e version catalog (libs.versions.toml)
├── build.gradle.kts   # Config Gradle root
├── settings.gradle.kts# Elenco dei moduli (shared, desktopApp)
└── gradle.properties  # Impostazioni Gradle/Kotlin
```

| Cartella | A cosa serve |
|---|---|
| `desktopApp` | Deve restare **sottile**: contiene solo `main()` e la creazione della finestra, che richiama `App()` del modulo `shared`. |
| `shared` | Contiene quasi tutto il codice. Si chiama "shared" perché in futuro potrebbe servire anche Android/iOS. |
| `build/`, `.gradle/`, `.idea/`, `.kotlin/` | Generate automaticamente. **Non toccarle e non committarle** (devono stare nel `.gitignore`). |

---

## 3. Le source set di `shared/src`

| Cartella | Contenuto |
|---|---|
| `commonMain` | Codice **uguale per tutte le piattaforme**. Qui sta il 95% del progetto. |
| `jvmMain` | Codice specifico per il desktop (JVM). Per Room: il **builder del database**. |
| `commonTest` | Test che girano su tutte le piattaforme. |
| `jvmTest` | Test specifici per la JVM (es. test del database in memoria). |

Il percorso Kotlin dentro `commonMain` è `kotlin/it/univaq/grr/fitslot/`, che è il tuo **package base**. Tutte le cartelle qui sotto si trovano dentro quel percorso.

---

## 4. Layer `data` — dove vive Room

Qui c'è tutto ciò che riguarda **salvare e leggere dati**. La UI non deve mai importare nulla da questa cartella.

### 4.1 `data/local/entity`

Le **tabelle** del database. Una classe = una tabella.

```kotlin
@Entity(tableName = "workout")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val createdAt: Long
)
```

- Nome consigliato: suffisso `Entity` (`WorkoutEntity`).
- Qui sono **ammesse** le annotazioni Room (`@Entity`, `@PrimaryKey`, `@ColumnInfo`, `@ForeignKey`, `@Index`).
- Non usare queste classi nella UI: per quello ci sono i modelli del `domain`.

### 4.2 `data/local/dao`

I **DAO** (Data Access Object): interfacce che dicono *come* interrogare le tabelle. Room genera il codice per te.

```kotlin
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun getById(id: Long): WorkoutEntity?

    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Delete
    suspend fun delete(workout: WorkoutEntity)
}
```

- Un DAO per ogni "area" (es. `WorkoutDao`, `SlotDao`).
- Funzioni di scrittura/lettura singola: `suspend`. Per osservare i dati nel tempo: restituisci `Flow`.
- Sono **interfacce**, non scrivi l'implementazione.

### 4.3 `data/local/converter`

I **ColumnTypeConverter** (in Room 3 si chiamano così, in Room 2 erano `@TypeConverter`): servono quando una proprietà ha un tipo che SQLite non conosce (enum, date, liste...) e va convertita in un tipo semplice (`String`, `Long`).

```kotlin
class Converters {
    @ColumnTypeConverter
    fun fromDayOfWeek(value: DayOfWeek): String = value.name

    @ColumnTypeConverter
    fun toDayOfWeek(value: String): DayOfWeek = DayOfWeek.valueOf(value)
}
```

Poi li registri sul database con `@ColumnTypeConverters(Converters::class)`.

### 4.4 `data/local/AppDatabase.kt`

Il **punto centrale** di Room: elenca entity e DAO.

```kotlin
@Database(
    entities = [WorkoutEntity::class, SlotEntity::class],
    version = 1,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun slotDao(): SlotDao
}

// Room genera l'implementazione "actual" su ogni piattaforma
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

- Ogni volta che **cambi le tabelle**, incrementa `version` (e scrivi una migration, oppure in sviluppo ricrea il DB).
- Con `exportSchema = true` Room salva lo schema in una cartella `schemas/` (configurata in Gradle): utile per le migration. **Committala su Git.**

### 4.5 `data/mapper`

Funzioni che **convertono** `Entity` ⇄ modello di dominio.

```kotlin
fun WorkoutEntity.toDomain() = Workout(
    id = id,
    name = name,
    duration = durationMinutes
)

fun Workout.toEntity() = WorkoutEntity(
    id = id,
    name = name,
    durationMinutes = duration,
    createdAt = Clock.System.now().toEpochMilliseconds()
)
```

Sono volutamente semplici: sono *extension function* in un file per area (`WorkoutMapper.kt`).

### 4.6 `data/repository`

Le **implementazioni** dei repository. Usano i DAO e i mapper, e restituiscono modelli di dominio.

```kotlin
class WorkoutRepositoryImpl(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override fun observeAll(): Flow<List<Workout>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(workout: Workout) {
        dao.insert(workout.toEntity())
    }
}
```

---

## 5. Layer `domain` — il cuore dell'app

Kotlin **puro**: niente Room, niente Compose, niente librerie di piattaforma.

### 5.1 `domain/models`

I **modelli** che rappresentano i concetti dell'app così come li pensi tu.

```kotlin
data class Workout(
    val id: Long = 0,
    val name: String,
    val duration: Int
)
```

Perché separarli dalle entity? Perché il DB può avere colonne extra, nomi diversi o tipi "tecnici" (timestamp come `Long`) che alla UI non interessano.

### 5.2 `domain/repository`

Le **interfacce** dei repository: dicono *cosa* si può fare, non *come*.

```kotlin
interface WorkoutRepository {
    fun observeAll(): Flow<List<Workout>>
    suspend fun add(workout: Workout)
}
```

La UI dipende da questa interfaccia; l'implementazione sta in `data/repository`.

### 5.3 `domain/usecase` *(opzionale, non ancora creata)*

Una classe per ogni azione dell'app (`AddWorkoutUseCase`, `BookSlotUseCase`...). Utile quando c'è logica di business (controlli, regole). Per un progetto piccolo puoi **saltarla** e chiamare direttamente i repository dai ViewModel.

---

## 6. Layer `ui` — Compose

Tutto ciò che si vede e con cui si interagisce.

### 6.1 `ui/screens`

Una **sottocartella per schermata**, con la schermata e il suo ViewModel insieme.

```
screens/
└── workoutlist/
    ├── WorkoutListScreen.kt    # @Composable della schermata
    ├── WorkoutListViewModel.kt # stato + logica della schermata
    └── WorkoutListUiState.kt   # data class con lo stato mostrato
```

Schema tipico: il ViewModel espone uno `StateFlow<UiState>`, la Screen lo osserva e disegna.

### 6.2 `ui/components`

**Composable riutilizzabili** usati in più schermate (bottoni custom, card, dialog, campi di testo...). Se un pezzo di UI serve in una sola schermata, lascialo nella cartella di quella schermata.

### 6.3 `ui/navigator`

La **navigazione** tra schermate: definizione delle rotte (di solito una `sealed class`/`sealed interface` o oggetti `@Serializable`) e il grafo di navigazione.

```kotlin
sealed interface Screen {
    data object WorkoutList : Screen
    data class WorkoutDetail(val id: Long) : Screen
}
```

### 6.4 `ui/theme`

**Aspetto grafico**: colori, tipografia, forme e il composable `FitSlotTheme { ... }` che avvolge l'intera app.

---

## 7. Altri file e cartelle

| Elemento | A cosa serve |
|---|---|
| `utils/` | Funzioni di utilità generiche (formattazione date, extension function, costanti). Non riempirla di tutto: se un helper appartiene a un'area precisa, mettilo lì. |
| `App.kt` | Il composable radice `App()`: applica il tema e mostra la navigazione. È chiamato da `desktopApp`. |
| `Platform.kt` | Tipico file `expect/actual` del template KMP per sapere su quale piattaforma si gira. Puoi tenerlo o eliminarlo. |
| `composeResources/` | Risorse di Compose Multiplatform: immagini, font, stringhe (`values/strings.xml`). |
| `di/` *(da creare)* | Dependency injection (es. Koin): qui dici "quando serve un `WorkoutRepository`, crea `WorkoutRepositoryImpl` con questo DAO". |

---

## 8. La parte specifica del desktop: `jvmMain`

Il database fisico (il file `.db`) si crea in modo diverso per ogni piattaforma, quindi il **builder** sta in `jvmMain`:

```
jvmMain/kotlin/it/univaq/grr/fitslot/data/local/
└── DatabaseBuilder.kt
```

Idea del contenuto:

```kotlin
fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".fitslot/fitslot.db")
    dbFile.parentFile.mkdirs()
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}
```

Poi, in un punto comune (es. `di/`), costruisci il database:

```kotlin
val database = getDatabaseBuilder()
    .setDriver(BundledSQLiteDriver())   // driver SQLite incluso, funziona su desktop
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
```

---

## 9. Configurazione Gradle per Room (promemoria)

In `shared/build.gradle.kts` ti servono, in sintesi:

1. **Plugin** `ksp` e plugin `androidx.room3` (per l'export degli schemi).
2. **Dipendenze** `room-runtime` e `sqlite-bundled` in `commonMain`.
3. **KSP per il target JVM** (`add("kspJvm", room-compiler)`), più `kspCommonMainMetadata` se il compilatore lo richiede.
4. Blocco `room3 { schemaDirectory("$projectDir/schemas") }`.

I nomi esatti e le versioni di Room 3 vanno presi dalla documentazione ufficiale.

---

## 10. Dove metto il nuovo codice? (tabella rapida)

| Voglio aggiungere... | Lo metto in... |
|---|---|
| Una nuova tabella | `data/local/entity` + aggiungila a `@Database` in `AppDatabase` |
| Una query | `data/local/dao` |
| Un tipo speciale (enum, data) da salvare | `data/local/converter` |
| La conversione entity ⇄ modello | `data/mapper` |
| Un concetto dell'app (es. `Workout`) | `domain/models` |
| Le operazioni disponibili su un concetto | `domain/repository` (interfaccia) |
| L'implementazione di quelle operazioni | `data/repository` |
| Una nuova schermata | `ui/screens/<nome>` |
| Un pezzo di UI riutilizzabile | `ui/components` |
| Una nuova rotta | `ui/navigator` |
| Colori / font | `ui/theme` |
| Qualcosa che cambia tra desktop e altre piattaforme | `jvmMain` (con `expect/actual`) |

---

## 11. Flusso completo di un esempio

Mostrare l'elenco degli allenamenti:

1. `WorkoutListScreen` (**ui**) osserva lo stato del `WorkoutListViewModel`.
2. Il ViewModel chiama `WorkoutRepository.observeAll()` (**interfaccia in domain**).
3. `WorkoutRepositoryImpl` (**data/repository**) chiama `WorkoutDao.observeAll()` (**data/local/dao**).
4. Room legge la tabella e restituisce `WorkoutEntity`.
5. Il repository li converte in `Workout` con i **mapper**.
6. Il ViewModel aggiorna lo stato e la Screen si ridisegna.

La UI non ha mai visto Room: è esattamente l'obiettivo di questa struttura.

---

## 12. Errori comuni da evitare

- Usare le `Entity` direttamente nella UI (crea dipendenza UI → Room).
- Mettere logica nei DAO: devono contenere solo query.
- Dimenticare di aggiungere una nuova entity all'elenco di `@Database`.
- Cambiare una tabella senza incrementare `version`.
- Riempire `utils` di codice che avrebbe un posto migliore altrove.
- Mettere in `commonMain` codice che usa API solo JVM (`java.io.File`, ecc.): va in `jvmMain`.
