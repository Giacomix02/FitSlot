# Capitolo 1: I Fondamenti di Kotlin per Neofiti

Benvenuto nella guida a **Kotlin** per il progetto **FitSlot**. Se hai studiato linguaggi tradizionali come C, C++ o Java scolastico, Kotlin potrebbe sembrarti diverso a prima vista. 

Questo capitolo spiega i concetti cardine del linguaggio che utilizzerai ogni giorno nel progetto, spiegati senza dare nulla per scontato e con esempi concreti.

---

## 1. Perché Kotlin?

Kotlin è un linguaggio moderno sviluppato da JetBrains (i creatori di IntelliJ IDEA) e adottato da Google come linguaggio ufficiale di riferimento.
I suoi tre obiettivi principali sono:
1. **Espressività e sintesi:** puoi scrivere in 5 righe quello che in Java richiedeva 50 righe di "boilerplate" (codice ripetitivo).
2. **Sicurezza (Null-Safety):** elimina alla radice il peggior incubo dei programmatori: il `NullPointerException`.
3. **Multiplatform:** lo stesso codice Kotlin gira su Desktop (Windows, macOS, Linux), Android, iOS e Web.

---

## 2. Immutabilità: `val` contro `var`

In Kotlin ci sono due modi per dichiarare una variabile:

```kotlin
val nome = "Giacomo"  // IMMUTABILE (Sola lettura)
var minuti = 45       // MUTABILE (Riassegnabile)

// nome = "Marco"     // ❌ ERRORE DI COMPILAZIONE! 'val' non può essere riassegnata.
minuti = 60           // ✅ Valido! 'var' può cambiare valore.
```

> [!IMPORTANT]
> **Best Practice di FitSlot:**
> Usa **`val` nel 95% dei casi**. Rende il codice prevedibile, evita bug nei thread concorrenti e rende gli stati dell'interfaccia facili da tracciare. Usa `var` solo quando strettamente necessario (ad esempio all'interno di un ciclo o in uno stato locale di Compose con `remember`).

---

## 3. La Null-Safety: Addio ai Crash a Runtime

In molti linguaggi, se una variabile è `null` e provi ad accedere a un suo metodo (es. `user.getName()`), il programma va in crash immediato (`NullPointerException` o segmentation fault).

In Kotlin il sistema dei tipi distingue rigidamente chi può essere `null` e chi non può esserlo:

```kotlin
var titolo: String = "Allenamento"
// titolo = null // ❌ ERRORE DI COMPILAZIONE! String non accetta mai null!

var note: String? = null // ✅ Il punto di domanda '?' indica che la variabile può contenere null
```

### Come si lavora con i valori nullable (`String?`):

#### 1. Safe Call (`?.`)
Esegue l'operazione solo se l'oggetto non è nullo, altrimenti restituisce `null` senza crashare:
```kotlin
val lunghezza: Int? = note?.length // Se note è null, lunghezza diventa null. Nessun crash!
```

#### 2. Elvis Operator (`?:`) - Il valore di default
Fornisce un valore alternativo se l'espressione a sinistra è `null`:
```kotlin
// Se 'note' è null, usa la stringa vuota ""
val testoVisualizzato: String = note ?: "Nessuna nota presente"
```

#### 3. Smart Cast (Verifica automatica)
Se controlli con un `if` che una variabile non è nulla, Kotlin la trasforma automaticamente in non-nulla dentro il blocco:
```kotlin
if (note != null) {
    // Qui dentro 'note' è automaticamente di tipo String (non più String?)
    println("Le note contengono ${note.length} caratteri")
}
```

---

## 4. `data class`: Creare Modelli con una Sola Riga

In Java o C++, per creare una classe che contiene dati (`Workout`) dovevi scrivere:
- Le variabili private
- Il costruttore
- Tutti i metodi `getter` e `setter`
- Il metodo `equals()`
- Il metodo `hashCode()`
- Il metodo `toString()`

In Kotlin basta scrivere **`data class`**:

```kotlin
data class Workout(
    val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val notes: String? = null
)
```

Kotlin genera automaticamente sotto il cofano:
- `toString()` leggibile: stampa `Workout(id=1, name=Panca, ...)` invece dell'indirizzo di memoria.
- `equals()` e `hashCode()`: due oggetti con gli stessi valori sono considerati uguali (`w1 == w2`).
- **Il metodo `.copy()`**: permette di creare una nuova istanza modificando solo alcuni campi, preservando l'immutabilità!

```kotlin
val allenamentoBase = Workout(id = 1, name = "Corsa", durationMinutes = 30)

// Creiamo un nuovo oggetto copiando il precedente ma cambiando solo la durata:
val allenamentoLungo = allenamentoBase.copy(durationMinutes = 60)
// allenamentoLungo ha id=1, name="Corsa", durationMinutes=60
```

---

## 5. `sealed interface` e `sealed class`: Gli Stati della UI

Cosa sono? Sono classi o interfacce con una gerarchia **chiusa e nota a tempo di compilazione**. 

Immagina di dover rappresentare lo stato di una schermata che carica i dati da Room:
- Sta caricando?
- Ha caricato i dati con successo?
- Si è verificato un errore?

Con una `sealed interface` puoi modellarlo in modo perfetto ed elegante:

```kotlin
sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Success(val workouts: List<Workout>) : WorkoutUiState
    data class Error(val message: String) : WorkoutUiState
}
```

### Perché sono una rivoluzione? Il `when` esaustivo!

Quando usi un'espressione `when` con una `sealed interface`, il compilatore Kotlin controlla che tu abbia gestito **tutti i casi possibili**:

```kotlin
@Composable
fun WorkoutScreen(state: WorkoutUiState) {
    when (state) {
        is WorkoutUiState.Loading -> {
            CircularProgressIndicator() // Mostra il cerchio di caricamento
        }
        is WorkoutUiState.Success -> {
            WorkoutList(state.workouts) // Mostra i dati
        }
        is WorkoutUiState.Error -> {
            Text("Errore: ${state.message}", color = Color.Red)
        }
        // Non serve un 'else'! Il compilatore sa che non esistono altri stati possibili.
    }
}
```
Se in futuro aggiungi un nuovo stato `data object Empty : WorkoutUiState`, il compilatore ti segnalerà in rosso tutti i punti dell'app in cui devi gestirlo!

---

## 6. Extension Functions: Estendere Classi Esistenti

Le **Funzioni di Estensione** permettono di aggiungere nuove funzioni a classi esistenti (anche classi standard di Kotlin o di librerie di terze parti) senza dover ereditare da esse.

Sintassi: `fun NomeClasse.nomeNuovaFunzione(...)`

Esempio pratico in FitSlot: convertire i minuti dalla mezzanotte in un orario leggibile "HH:mm":

```kotlin
// In utils/TimeUtils.kt
fun Int.toHourMinuteFormat(): String {
    val ore = this / 60
    val minuti = this % 60
    // String.format per formattare a due cifre: es. 9 -> "09", 5 -> "05"
    return "${ore.toString().padStart(2, '0')}:${minuti.toString().padStart(2, '0')}"
}

// Nel tuo codice puoi usarlo come se fosse un metodo nativo di Int!
val startMinute = 630
println(startMinute.toHourMinuteFormat()) // Stampa: "10:30"
```

Usiamo le extension functions anche per i nostri **Mapper** (vedi Capitolo 3):
```kotlin
fun WorkoutEntity.toDomain(): Workout = Workout(id = id, name = name, durationMinutes = durationMinutes)
```

---

## 7. Coroutine in Breve: `suspend` e `viewModelScope`

Nei computer, il thread principale (chiamato **Main Thread** o UI Thread) si occupa di disegnare i pixel sullo schermo a 60 o 120 frame al secondo.
Se su quel thread provi a leggere 10.000 righe da SQLite, lo schermo si "congela" e l'app non risponde più ai click dell'utente.

Le **Coroutine** sono "thread leggeri" che permettono di fare operazioni pesanti in background senza bloccare la grafica.

- **`suspend fun`**: Una funzione che può essere messa in pausa (mentre attende che il disco legga i dati) e ripresa in seguito, senza bloccare il thread!
- **`viewModelScope.launch { ... }`**: Il modo standard con cui un ViewModel avvia una coroutine per eseguire una chiamata al database.

```kotlin
class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {
    fun add(workout: Workout) {
        // Avviamo una coroutine legata al ciclo di vita del ViewModel
        viewModelScope.launch {
            // Questa chiamata è sospensiva: aspetta che SQLite abbia salvato,
            // ma nel frattempo la UI continua a rispondere ai click dell'utente!
            repository.addWorkout(workout)
        }
    }
}
```

---

## 8. Riepilogo del Capitolo

- Usa **`val`** come scelta predefinita per rendere i dati immutabili.
- La **Null-Safety** (`String` vs `String?`) previene i crash a runtime grazie all'operatore elvis (`?:`) e safe-call (`?.`).
- Le **`data class`** eliminano il codice ripetitivo e offrono il metodo `.copy()`.
- Le **`sealed interface`** sono lo strumento ideale per modellare stati ed eventi della UI gestiti con `when` esaustivo.
- Le **Extension Functions** consentono di arricchire classi esistenti con funzioni eleganti e leggibili.
- Le funzioni **`suspend`** e le coroutine consentono di interrogare Room senza mai bloccare la UI desktop.

Nel prossimo capitolo analizzeremo l'albero delle cartelle del progetto FitSlot e risponderemo alla domanda più frequente dei principianti: **"Dove devo mettere questo codice?"**.
