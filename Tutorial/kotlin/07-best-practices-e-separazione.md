# Capitolo 7: Best Practices, Separazione delle Responsabilità e Anti-Pattern

Quando si lavora a un progetto software in team (specialmente in contesti accademici o professionali con metodologie agili), è facile che il codice diventi disordinato dopo poche settimane.

Questo capitolo raccoglie le **regole architetturali fondamentali**, i **pattern consigliati** e gli **anti-pattern da evitare a tutti i costi**, per garantire che il codice di **FitSlot** rimanga sempre pulito, testabile e comprensibile per tutti i membri del gruppo.

---

## 1. Il Principio della Responsabilità Singola (SRP)

Ogni file e ogni classe nel tuo progetto deve avere **un solo motivo per cambiare**:

| Componente | Cosa DEVE fare | Cosa NON deve MAI fare |
|---|---|---|
| **`@Entity`** | Descrivere la tabella SQLite e i tipi di colonna. | Non deve contenere logica di calcolo o formattazione. |
| **`@Dao`** | Definire ed eseguire query SQL. | Non deve sapere come i dati vengono mostrati a schermo. |
| **`Mapper`** | Tradurre da Entity a Domain Model e viceversa. | Non deve fare controlli di validazione o chiamate di rete. |
| **`Repository`** | Esporre flussi puliti (`Flow<List<T>>`) e coordinare i dati. | Non deve manipolare direttamente widget o stati grafici. |
| **`ViewModel`** | Mantenere lo stato della schermata (`StateFlow`) e rispondere agli eventi utente. | Non deve contenere codice Compose (`@Composable`, `Modifier`, `Color`) né query SQL. |
| **`Screen` (@Composable)** | Disegnare l'interfaccia grafica a partire dallo stato ricevuto. | Non deve aprire connessioni al database, né fare calcoli pesanti. |

---

## 2. Unidirectional Data Flow (UDF): Il Flusso a Senso Unico

Per evitare stati incoerenti o bug impossibili da riprodurre, FitSlot adotta il pattern **UDF (Flusso di Dati Unidirezionale)**:

```
          ┌──────────────────────────┐
          │        ViewModel         │
          └─────────────┬────────────┘
                        │
                  Lo STATO scende
              StateFlow<WorkoutUiState>
                        │
                        ▼
          ┌──────────────────────────┐
          │      Compose Screen      │
          └─────────────┬────────────┘
                        │
                 Gli EVENTI salgono
               onClick() -> onEvent()
                        │
                        ▼
          ┌──────────────────────────┐
          │        ViewModel         │
          └──────────────────────────┘
```

1. **Lo stato scende (State goes down):** Il ViewModel espone un singolo oggetto di stato immutabile (`UiState`). La schermata si limita a leggerlo.
2. **Gli eventi salgono (Events go up):** Quando l'utente preme un pulsante, la schermata non modifica lo stato da sola: invia un messaggio di evento al ViewModel (`viewModel.onDeleteClicked(id)`).
3. **Il ViewModel aggiorna lo stato:** Il ViewModel elabora l'evento, esegue l'operazione sul Repository e genera un nuovo stato.

---

## 3. I 4 Grandi Anti-Pattern da Evitare

### ❌ Anti-Pattern 1: Il "God Composable" (La Schermata che fa Tutto)
Scrivere un file da 800 righe in cui una funzione `@Composable` gestisce i campi di testo, parla con il database, valida i dati e disegna la grafica.
- **Soluzione:** Spezza la schermata! Crea il `ViewModel` per la logica, sposta i bottoni e i dialog in `ui/components/` e mantieni la schermata principale snella (massimo 150-200 righe).

---

### ❌ Anti-Pattern 2: Modificare le Liste in Loco (In-Place Mutation)
```kotlin
// ❌ PESSIMO: Modificare una lista mutabile
val lista = mutableListOf<Workout>()
lista.add(nuovoWorkout) // Compose NON si accorgerà del cambiamento perché l'indirizzo della lista è lo stesso!

// ✅ CORRETTO: Creare una nuova lista immutabile
val nuovaLista = listaAttuale + nuovoWorkout
```
In Compose, la ricomposizione scatta solo quando l'oggetto referenziato cambia identità. Usa sempre **liste immutabili (`List<T>`)** e funzioni come `+`, `-` o `.filter()`.

---

### ❌ Anti-Pattern 3: Lanciare Operazioni Asincrone Direttamente nel Composable
```kotlin
// ❌ PESSIMO: Avviare coroutine nel corpo libero della UI
@Composable
fun BadScreen(dao: WorkoutDao) {
    // Questo codice verrà eseguito a OGNI singolo frame di ricomposizione!
    // Finirai per lanciare migliaia di query al secondo mandando l'app in crash!
    GlobalScope.launch {
        dao.insert(...)
    }
}

// ✅ CORRETTO: Le chiamate asincrone vanno nelle funzioni del ViewModel
Button(onClick = { viewModel.addWorkout(...) }) {
    Text("Aggiungi")
}
```

---

### ❌ Anti-Pattern 4: Saltare il Repository
Chiamare direttamente il `WorkoutDao` all'interno del `ViewModel` o della schermata Compose.
- **Perché è sbagliato?** Il DAO restituisce `WorkoutEntity`, costringendo il ViewModel a conoscere le annotazioni del database. Se cambi una colonna di SQLite, romperai sia il database, sia il ViewModel, sia la UI. Il `Repository` esiste proprio per fare da scudo protettivo!

---

## 4. Checklist di Qualità del Codice (Prima di Fare Commit o PR)

Prima di committare il tuo codice su Git o inviare una Pull Request al team, verifica questi punti:

- [ ] **Zero codice Room nella UI:** La cartella `ui/` non importa classi con annotazioni `@Entity` o `@Dao`.
- [ ] **Zero codice UI nel Domain:** La cartella `domain/` non importa `@Composable`, `Modifier` o librerie esterne.
- [ ] **Uso di `val` invece di `var`:** Tutte le proprietà dei modelli e degli stati sono immutabili.
- [ ] **State Hoisting applicato:** I componenti riutilizzabili in `ui/components/` accettano `Modifier` e usano lambda per gli eventi (`onClick: () -> Unit`).
- [ ] **Gestione delle stringhe nulle:** Non ho usato operatori pericolosi come `!!` (double bang assertion). Ho usato `?.` o `?:` per gestire i valori nulli.
- [ ] **LazyColumn ottimizzata:** Ogni `LazyColumn` specifica il parametro `key = { it.id }`.
- [ ] **Separazione dei file:** Non ho messo più di una classe principale nello stesso file sorgente.

---

## 5. Riepilogo del Corso Kotlin & Architettura

Congratulazioni! Con questi 7 capitoli hai ora una visione completa e chiara di come è strutturato **FitSlot**:
- Conosci la sintassi e i punti di forza di **Kotlin** (null-safety, `data class`, `sealed interface`, coroutine).
- Sai esattamente **dove mettere ogni file** nella struttura del repository.
- Comprendi come il dato viaggia reattivamente **da SQLite allo schermo** senza codice di refresh manuale.
- Sai come gestire la **navigazione Desktop type-safe**, i **temi Material 3** e la **creazione di componenti riutilizzabili**.
- Possiedi le **best practice architetturali** per evitare il disordine e lavorare in modo professionale con il tuo team agile.
