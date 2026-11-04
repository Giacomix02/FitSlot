# Capitolo 9: Best Practices e Risoluzione degli Errori Comuni

In questo capitolo conclusivo raccogliamo le **10 Regole d'Oro** per scrivere codice professionale con Room 3 in Kotlin Multiplatform, seguite da una guida dettagliata alla **diagnostica e risoluzione di tutti gli errori più frequenti** in cui si imbattono i principianti.

---

## 1. I 10 Comandamenti di Room 3 in KMP

1. **Il Database deve essere un Singleton:** Non chiamare mai `createRoomDatabase()` dentro una schermata o un componente. Istanzialo una sola volta all'avvio dell'applicazione desktop e condividilo tra i repository.
2. **Nessun metodo bloccante nei DAO:** Tutte le funzioni nel DAO devono essere `suspend fun` oppure restituire `Flow<T>`. Room 3 rifiuta qualsiasi chiamata sincrona che rischi di bloccare l'interfaccia utente.
3. **Le Entity non vanno mai nella UI:** La UI deve conoscere solo i modelli del `domain`. Usa sempre i **Mapper** (`toDomain()` e `toEntity()`) per separare la struttura del database dalla visualizzazione grafica.
4. **Metti sempre un `@Index` sulle chiavi esterne:** Se in `SlotEntity` hai `val workoutId: Long`, definisci `indices = [Index("workout_id")]`. Senza indice, ogni ricerca su quella relazione costringerà SQLite a una lenta scansione completa della tabella.
5. **Non concatenare mai stringhe nelle query:** Usa sempre i parametri `:nomeParametro` (es. `WHERE name = :name`). Eviterai bug di sintassi e proteggerai l'app da vulnerabilità di **SQL Injection**.
6. **Committa la cartella `schemas/` su Git:** Il compilatore di Room ha bisogno dello storico degli schemi JSON per convalidare le `autoMigrations`. Non inserire mai la cartella `schemas/` nel file `.gitignore`.
7. **Usa sempre `BundledSQLiteDriver`:** Non affidarti ai driver di sistema installati sul computer dell'utente. Il driver bundled garantisce che la stessa versione di SQLite giri identica su Windows 10/11, macOS e distribuzioni Linux.
8. **Esegui le query su `Dispatchers.IO`:** Configura sempre `.setQueryCoroutineContext(Dispatchers.IO)` nel builder del database per non rubare cicli di calcolo al thread di rendering di Compose.
9. **Usa `WhileSubscribed(5_000)` nei ViewModel:** Quando converti il `Flow` del DAO in `StateFlow`, usa sempre `SharingStarted.WhileSubscribed(5000)` per rilasciare le risorse quando la schermata non è visibile.
10. **Tieni semplici i Type Converter:** Se hai solo un timestamp, memorizzalo come `Long` nell'Entity e convertilo in una data leggibile nel Mapper. Riduci al minimo indispensabile i convertitori complessi.

---

## 2. Tabella Diagnostica: Errori Comuni e Soluzioni

Trova nella colonna di sinistra l'errore che visualizzi nel log di compilazione o nella console, e segui la soluzione corrispondente:

| Messaggio di Errore | Causa Reale | Soluzione Immediata |
|---|---|---|
| `Expected object 'AppDatabaseConstructor' has no actual declaration` | È un falso positivo dell'editor di IntelliJ prima della compilazione KSP. | Aggiungi `@Suppress("KotlinNoActualForExpect")` sopra la riga `expect object AppDatabaseConstructor`. |
| `Unresolved reference: AppDatabase_Impl` | KSP non ha generato il codice per il target JVM del desktop. | In `shared/build.gradle.kts`, verifica di aver inserito `dependencies { add("kspJvm", libs.androidx.room3.compiler) }`. |
| `Schema export directory is not provided to the annotation processor...` | Manca l'indicazione della cartella per salvare gli schemi JSON. | In `shared/build.gradle.kts`, aggiungi il blocco: `room3 { schemaDirectory("$projectDir/schemas") }`. |
| `Cannot figure out how to save this field into database` | Un campo nell'Entity ha un tipo che SQLite non conosce nativamente (es. una data, un enum o una lista). | Rendi il tipo primitivo (es. `Long` o `String`), oppure scrivi una funzione annotata con `@ColumnTypeConverter` e registrala nel database. |
| `Query method parameters should either be a type that can be converted...` | Hai dimenticato la parola chiave `suspend` prima del nome della funzione nel DAO, oppure non restituisci un `Flow`. | In Room 3 le funzioni DAO devono essere `suspend fun` oppure restituire `Flow<...>`. |
| `Unresolved reference: androidx.room.*` | Hai copiato codice da un tutorial vecchio basato su Room 2. | Sostituisci tutti gli import con il nuovo package: `androidx.room3.*`. |
| `@TypeConverter / @TypeConverters cannot be applied...` | Nomi di annotazioni obsoleti appartenenti a Room 2. | In Room 3 si chiamano `@ColumnTypeConverter` e `@ColumnTypeConverters`. |
| `Cannot find symbol: withTransaction` | Il vecchio metodo monolitico di Room 2 è stato rimpiazzato. | Usa `database.withWriteTransaction { }` (per modifiche) o `database.withReadTransaction { }` (per letture). |
| `Room cannot verify the data integrity. Looks like you've changed schema...` | Hai modificato una classe `@Entity` (aggiunto/rimosso campi) senza aggiornare la versione del DB. | 1. Incrementa `version` in `@Database(version = N+1)`.<br>2. Scrivi una migrazione, oppure in fase di sviluppo cancella il file locale `.db` in `~/.fitslot/fitslot.db`. |
| `SQLiteConstraintException: FOREIGN KEY constraint failed` | Stai tentando di inserire uno `SlotEntity` con un `workoutId` che non esiste nella tabella `workout`, oppure cancelli un workout senza la clausola CASCADE. | Assicurati che l'ID padre esista prima di inserire i figli, oppure imposta `onDelete = ForeignKey.CASCADE`. |
| `SQLiteException: database is locked` | Hai creato più di un'istanza di `AppDatabase` contemporaneamente che provano a scrivere sullo stesso file. | Trasforma `AppDatabase` in un **Singleton** unico condiviso in tutta l'applicazione. |
| `KSP version mismatch with Kotlin version` | La versione del plugin KSP in `libs.versions.toml` non supporta la versione del compilatore Kotlin. | Consulta la tabella di compatibilità ufficiale di KSP e allinea le due versioni in `libs.versions.toml`. |
| La UI Compose non si aggiorna dopo aver inserito un dato | La query nel DAO restituisce una lista fissa (`suspend fun getAll(): List<T>`) invece di un `Flow`. | Cambia la firma della query nel DAO in `fun observeAll(): Flow<List<T>>`. |
| I dati inseriti spariscono ogni volta che riavvii l'applicazione | Hai usato `java.io.tmpdir` oppure `inMemoryDatabaseBuilder` nel codice di produzione. | Nel builder desktop (`DatabaseBuilder.jvm.kt`), specifica un file stabile all'interno della cartella dell'utente: `System.getProperty("user.home") + "/.fitslot/fitslot.db"`. |

---

## 3. Checklist Pre-Volo per il Neofita

Prima di eseguire `./gradlew run` o avviare l'applicazione desktop, fai questo rapido controllo mentale:

- [ ] Ho messo `@PrimaryKey(autoGenerate = true)` su ogni `@Entity` con un default `val id: Long = 0`?
- [ ] Ho inserito tutte le mie classi Entity nell'elenco `@Database(entities = [..., ...])`?
- [ ] Le funzioni del mio DAO sono tutte `suspend` o restituiscono un `Flow`?
- [ ] Ho aggiunto l'annotazione `@Transaction` sulle query che caricano relazioni con `@Relation`?
- [ ] In `shared/build.gradle.kts` è presente `add("kspJvm", libs.androidx.room3.compiler)` e il blocco `room3 { schemaDirectory(...) }`?
- [ ] Ho impostato il database come Singleton in modo da non chiamare `.build()` ad ogni click dell'utente?
- [ ] I miei componenti grafici Compose usano modelli del `domain` e non le `Entity` del database?

Se hai spuntato tutte le voci, la tua architettura con Room 3 è robusta, reattiva e pronta per la produzione!
