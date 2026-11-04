# FitSlot — Portale Tutorial e Guide per Sviluppatori

Benvenuto nella documentazione tecnica e didattica del progetto **FitSlot** (sviluppato con **Kotlin Multiplatform**, **Compose Desktop** e **Room 3**).

Questa cartella è una guida completa pensata per accompagnare studenti e sviluppatori neofiti passo dopo passo: dai concetti base di Kotlin e dei database relazionali, all'architettura software a strati (Clean Architecture), fino all'interfaccia grafica reattiva con Compose Desktop.

---

## 🗺️ Mappa della Documentazione

```
Tutorial/
├── README.md                              ◄── (Sei qui) Portale e Indice Generale
├── struttura-progetto-fitslot.md          ◄── Guida alla struttura delle cartelle e Clean Architecture
├── room3-guida-per-principianti.md        ◄── Bignami e sintesi rapida di Room 3
│
├── kotlin/                                ◄── CORSO KOTLIN, ARCHITETTURA E UI COMPOSE (7 Capitoli)
│   ├── 01-fondamenti-di-kotlin.md         ◄── Immutabilità (val/var), Null-Safety, data class, sealed interface
│   ├── 02-struttura-cartelle-e-guida-dove-metto.md ◄ Guida "Dove metto questo codice?", albero decisionale e import
│   ├── 03-il-flusso-dati-completo-db-to-ui.md ◄── Il filo rosso da SQLite allo schermo Compose (lettura e scrittura)
│   ├── 04-interfaccia-grafica-compose-desktop.md ◄ Modello dichiarativo, stato (remember), ricomposizione, State Hoisting
│   ├── 05-routing-e-navigazione.md        ◄── Routing type-safe su Desktop con sealed interface e Navigator
│   ├── 06-temi-e-componenti-riutilizzabili.md ◄── Palette Material 3, Dark Mode, bottoni e dialog riutilizzabili
│   └── 07-best-practices-e-separazione.md ◄── Responsabilità singola (SRP), UDF, anti-pattern e checklist qualità
│
└── room3/                                 ◄── CORSO COMPLETO ROOM 3 (9 Capitoli)
    ├── 01-fondamenti-e-architettura.md    ◄── RAM vs Disco, SQLite, ORM e novità di Room 3
    ├── 02-configurazione-gradle-e-kmp.md  ◄── libs.versions.toml, KSP e build.gradle.kts
    ├── 03-entita-e-relazioni.md           ◄── @Entity, PrimaryKey, Foreign Keys, 1-N, Type Converters
    ├── 04-dao-e-query.md                  ◄── Interfacce DAO, CRUD, Query SQL, suspend e Flow
    ├── 05-database-e-desktop.md           ◄── AppDatabase, @ConstructedBy, builder Desktop JVM e Singleton
    ├── 06-architettura-clean-e-ui.md      ◄── Entity -> Mapper -> Domain -> Repository -> ViewModel -> Compose
    ├── 07-transazioni-e-migrazioni.md     ◄── Transazioni ACID, Schemas JSON, AutoMigrations e Migrazioni manuali
    ├── 08-testing-del-database.md         ◄── Test In-Memory con coroutine, runTest e test dei DAO
    └── 09-best-practices-e-errori-comuni.md ◄ Tabella diagnostica errori, 10 comandamenti e checklist
```

---

## 🧭 Percorsi di Apprendimento Consigliati

### Percorso A: "Voglio capire come funziona Kotlin e dove mettere i file"
Se sei nuovo di Kotlin o del progetto FitSlot, inizia qui:
1. [Kotlin Capitolo 1: I Fondamenti di Kotlin](kotlin/01-fondamenti-di-kotlin.md) (Null-Safety, data class, sealed interface).
2. [Kotlin Capitolo 2: Struttura Cartelle e Guida 'Dove Metto Questo?'](kotlin/02-struttura-cartelle-e-guida-dove-metto.md) (Albero decisionale e matrice degli import vietati).
3. [Kotlin Capitolo 3: Il Flusso Dati Completo](kotlin/03-il-flusso-dati-completo-db-to-ui.md) (Come i dati viaggiano dal disco allo schermo).
4. [Kotlin Capitolo 4: Come Funziona Compose Desktop](kotlin/04-interfaccia-grafica-compose-desktop.md) (Stato, ricomposizione e State Hoisting).
5. [Kotlin Capitolo 5: Routing e Navigazione](kotlin/05-routing-e-navigazione.md) (Cambio pagina type-safe e gestione stack).
6. [Kotlin Capitolo 6: Temi e Componenti Riutilizzabili](kotlin/06-temi-e-componenti-riutilizzabili.md) (Material 3 e componenti modulari).
7. [Kotlin Capitolo 7: Best Practices e Anti-Pattern](kotlin/07-best-practices-e-separazione.md) (Separazione pulita e checklist).

### Percorso B: "Voglio padroneggiare il Database (Room 3)"
Se devi creare o modificare tabelle, query o migrazioni:
1. [Room 3 Capitolo 1: Fondamenti e Architettura](room3/01-fondamenti-e-architettura.md) (SQLite, ORM e novità Room 3).
2. [Room 3 Capitolo 2: Configurazione Gradle e KSP](room3/02-configurazione-gradle-e-kmp.md) (libs.versions.toml e setup KMP).
3. [Room 3 Capitolo 3: Entity, Relazioni e Converters](room3/03-entita-e-relazioni.md) (Tabelle, Foreign Keys, 1-N).
4. [Room 3 Capitolo 4: DAO e Query Reattive](room3/04-dao-e-query.md) (CRUD, suspend e Flow).
5. [Room 3 Capitolo 5: Database e Builder Desktop](room3/05-database-e-desktop.md) (AppDatabase, user.home e Singleton).
6. [Room 3 Capitolo 6: Clean Architecture e UI](room3/06-architettura-clean-e-ui.md) (Mapper, Repository, ViewModel).
7. [Room 3 Capitolo 7: Transazioni e Migrazioni](room3/07-transazioni-e-migrazioni.md) (ACID, schemi JSON e AutoMigrations).
8. [Room 3 Capitolo 8: Testing del Database](room3/08-testing-del-database.md) (Test in-memory con coroutine).
9. [Room 3 Capitolo 9: Best Practices ed Errori Comuni](room3/09-best-practices-e-errori-comuni.md) (Tabella diagnostica errori).

---

## 📌 Indice dei Moduli Kotlin, Architettura e UI

| Modulo | Argomenti Principali | Link Diretto |
|---|---|---|
| **01. Fondamenti di Kotlin** | Immutabilità (`val`/`var`), Null-Safety (`?.`, `?:`), `data class`, `sealed interface`, extension functions, coroutine | [Apri Capitolo 1](kotlin/01-fondamenti-di-kotlin.md) |
| **02. Dove Metto Questo Codice?** | Albero delle cartelle di FitSlot, guida decisionale per 12+ casi d'uso, matrice degli import vietati | [Apri Capitolo 2](kotlin/02-struttura-cartelle-e-guida-dove-metto.md) |
| **03. Flusso Dati Completo** | La pipeline unificata da SQLite a Compose: Entity -> Mapper -> Domain -> Repository -> ViewModel (`StateFlow`) -> Screen | [Apri Capitolo 3](kotlin/03-il-flusso-dati-completo-db-to-ui.md) |
| **04. Compose Desktop** | Paradigma dichiarativo vs imperativo, `remember`, `mutableStateOf`, ricomposizione, State Hoisting, `LazyColumn` con `key` | [Apri Capitolo 4](kotlin/04-interfaccia-grafica-compose-desktop.md) |
| **05. Routing e Navigazione** | Navigazione su Desktop con `sealed interface Screen`, gestione dello stack (`Navigator`), router in `App.kt` | [Apri Capitolo 5](kotlin/05-routing-e-navigazione.md) |
| **06. Temi e Componenti** | Palette Material 3 (`Color.kt`, `Type.kt`), Dark Mode (`FitSlotTheme`), creazione di bottoni, dialog e viste vuote modulari | [Apri Capitolo 6](kotlin/06-temi-e-componenti-riutilizzabili.md) |
| **07. Best Practices e Separazione** | Principio di responsabilità singola (SRP), Flusso unidirezionale (UDF), 4 grandi anti-pattern da evitare, checklist qualità | [Apri Capitolo 7](kotlin/07-best-practices-e-separazione.md) |

---

## 📌 Indice dei Moduli Room 3

| Modulo | Argomenti Principali | Link Diretto |
|---|---|---|
| **01. Fondamenti Room 3** | RAM vs Disco, SQLite, concetti di ORM, differenze Room 2 vs 3, i tre pilastri | [Apri Capitolo 1](room3/01-fondamenti-e-architettura.md) |
| **02. Setup Gradle e KSP** | `libs.versions.toml`, `shared/build.gradle.kts`, `kspJvm`, `BundledSQLiteDriver`, `schemaDirectory` | [Apri Capitolo 2](room3/02-configurazione-gradle-e-kmp.md) |
| **03. Entity e Relazioni** | `@Entity`, `@PrimaryKey`, `@ColumnInfo`, chiavi esterne, relazioni con `@Relation`, `@ColumnTypeConverter` | [Apri Capitolo 3](room3/03-entita-e-relazioni.md) |
| **04. DAO e Query Reattive** | CRUD completo, `@Upsert`, `suspend fun` vs `Flow`, query SQL sicure, `@Transaction` | [Apri Capitolo 4](room3/04-dao-e-query.md) |
| **05. Database e Desktop** | `RoomDatabase`, `@ConstructedBy` per KMP, builder Desktop JVM (`user.home`), Singleton Pattern | [Apri Capitolo 5](room3/05-database-e-desktop.md) |
| **06. Clean Architecture e UI** | Entity vs Domain Model, Mapper, Repository, ViewModel con `StateFlow`, rendering Compose | [Apri Capitolo 6](room3/06-architettura-clean-e-ui.md) |
| **07. Transazioni e Migrazioni** | Transazioni ACID (`withWriteTransaction`), schemi JSON, `autoMigrations`, migrazioni con `SQLiteConnection` | [Apri Capitolo 7](room3/07-transazioni-e-migrazioni.md) |
| **08. Testing del Database** | Test in memoria RAM con `inMemoryDatabaseBuilder`, `runTest`, verifica di chiavi esterne e Flow | [Apri Capitolo 8](room3/08-testing-del-database.md) |
| **09. Best Practices ed Errori** | 10 Comandamenti di Room 3, tabella diagnostica di 15+ errori frequenti con soluzione, checklist | [Apri Capitolo 9](room3/09-best-practices-e-errori-comuni.md) |
