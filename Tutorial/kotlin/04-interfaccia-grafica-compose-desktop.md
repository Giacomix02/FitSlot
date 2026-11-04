# Capitolo 4: Come Funziona l'Interfaccia Grafica (Compose Desktop)

In questo capitolo esploriamo il funzionamento interno di **Compose Multiplatform (Desktop)**. Se hai esperienza con vecchi framework per interfacce desktop (come Java Swing, JavaFX, Windows Forms o HTML/JavaScript manipolato con jQuery), scoprirai un paradigma completamente diverso: la **Programmazione Dichiarativa**.

---

## 1. Imperativo vs Dichiarativo: La Vera Differenza

### Il Vecchio Modo: Approccio Imperativo (es. Java Swing / JavaFX)
Nel modello imperativo dovevi dire al computer **passo dopo passo cosa fare e come modificare i widget**:

```kotlin
// ❌ Vecchio modo imperativo (pieno di bug di sincronizzazione)
fun onSalvaPremuto() {
    pulsante.setEnabled(false)
    etichetta.setText("Salvataggio in corso...")
    caricamentoSpinner.setVisible(true)
    
    // Se ti dimentichi una riga, la grafica rimane incoerente!
}
```

### Il Nuovo Modo: Approccio Dichiarativo (Compose Desktop)
In Compose non manipoli i pulsanti o le etichette. **Descrivi semplicemente come deve apparire l'interfaccia grafica per un determinato Stato**:

$$\text{UI} = f(\text{State})$$

L'interfaccia utente è una pura funzione matematica dello stato. Se lo stato cambia, Compose si occupa automaticamente di aggiornare lo schermo:

```kotlin
// ✅ Modo dichiarativo di Compose
@Composable
fun PulsanteSalva(isSaving: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSaving // Se isSaving è true, il bottone si disabilita da solo!
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            Text(" Salvataggio...")
        } else {
            Text("Salva")
        }
    }
}
```

---

## 2. L'Annotazione `@Composable`

Una funzione annotata con **`@Composable`** non è una funzione normale che restituisce un valore. È una funzione che dice al compilatore Compose: *"Questa funzione produce un pezzetto di interfaccia grafica"*.

Regole per le funzioni Composable:
1. Possono essere chiamate **solo** all'interno di altre funzioni `@Composable`.
2. Hanno la prima lettera **Maiuscola** per convenzione (`WorkoutListScreen()`, `CustomButton()`).
3. Possono essere eseguite molte volte di seguito (fenomeno della **Ricomposizione**).

---

## 3. Gestire lo Stato: `remember` e `mutableStateOf`

Cosa succede a una normale variabile locale all'interno di una funzione Composable?
Ogni volta che la funzione viene ridisegnata, la variabile verrebbe reimpostata al valore iniziale.

Per mantenere un valore in memoria tra una ridisegnazione e l'altra, usiamo la combinazione di **`remember`** e **`mutableStateOf`**:

```kotlin
@Composable
fun ContatoreSlot() {
    // remember dice a Compose: "Conserva questo valore anche quando ridisegni la funzione!"
    // mutableStateOf dice a Compose: "Se questo valore cambia, ridisegna immediatamente chi lo legge!"
    var postiDisponibili by remember { mutableStateOf(10) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { if (postiDisponibili > 0) postiDisponibili-- }) {
            Text("Prenota")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("Posti liberi: $postiDisponibili")
    }
}
```

> [!NOTE]
> Per usare la parola chiave `by` invece di `.value`, ricordati di importare:
> `import androidx.compose.runtime.getValue`
> `import androidx.compose.runtime.setValue`

---

## 4. La Ricomposizione Intelligente

La **Ricomposizione (Recomposition)** è il processo con cui Compose ri-esegue le tue funzioni per aggiornare lo schermo quando uno stato cambia.

Molti neofiti temono che ridisegnare l'interfaccia sia lento. Ma Compose usa un albero virtuale intelligente:
- Se hai una schermata con 100 elementi e cambia solo il testo di una card, **Compose salta i 99 elementi che non sono cambiati** e ri-esegue solo ed esclusivamente la funzione che legge il testo modificato!

### La Regola d'Oro per la Ricomposizione:
> **Non inserire operazioni lente o chiamate di rete/database direttamente nel corpo di una funzione `@Composable`!** 
> Poiché la funzione può essere ricompilata molte volte al secondo (ad esempio durante un'animazione o mentre l'utente ridimensiona la finestra dell'app desktop), il corpo della funzione deve essere veloce ed esente da effetti collaterali (Side Effects). Le operazioni pesanti vanno sempre nel ViewModel!

---

## 5. State Hoisting (Sollevamento dello Stato)

Uno dei concetti più importanti per scrivere componenti puliti e riutilizzabili in Compose è lo **State Hoisting**.

Significa: **sposta lo stato verso l'alto (nel genitore) e trasforma il componente figlio in un componente "stateless" (senza stato interno)**.

### Esempio: Campo di testo per l'inserimento del nome workout

```kotlin
// ❌ COMPONENTE STATICO DIFFICILED DA RIUTILIZZARE: Gestisce il proprio stato all'interno
@Composable
fun BadWorkoutInput() {
    var text by remember { mutableStateOf("") }
    TextField(value = text, onValueChange = { text = it })
}

// ✅ COMPONENTE STATELESS RIUTILIZZABILE (Best Practice!)
// Riceve il valore dall'esterno ed emette un evento quando cambia
@Composable
fun FitSlotTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
    )
}
```

Ora puoi riutilizzare `FitSlotTextField` per qualsiasi cosa: per il nome del workout, per la durata, per le note o per i dati utente, controllando il valore dal chiamante!

---

## 6. I Mattoni Fondamentali del Layout Desktop

In Compose l'allineamento e il posizionamento degli elementi si basa su tre contenitori standard:

```
┌─────────────────┐       ┌────────────────────────┐       ┌─────────────────┐
│     Column      │       │          Row           │       │       Box       │
│ ┌─────────────┐ │       │ ┌─────┐ ┌─────┐ ┌────┐ │       │ ┌─────────────┐ │
│ │  Elemento 1 │ │       │ │ El1 │ │ El2 │ │El3 │ │       │ │  Sotto      │ │
│ ├─────────────┤ │       │ └─────┘ └─────┘ └────┘ │       │ │ ┌─────────┐ │ │
│ │  Elemento 2 │ │       └────────────────────────┘       │ │ │ Sopra   │ │ │
│ └─────────────┘ │                                        │ │ └─────────┘ │ │
└─────────────────┘                                        └─────────────────┘
```

1. **`Column`**: Dispone i componenti in verticale (uno sotto l'altro).
2. **`Row`**: Dispone i componenti in orizzontale (uno accanto all'altro).
3. **`Box`**: Sovrappone i componenti l'uno sopra l'altro (utile per sfondi, badge o indicatori di caricamento centrati).

---

## 7. `LazyColumn` e l'Uso Obbligatorio della `key`

Per mostrare liste di decine o centinaia di elementi su Desktop, **non usare mai un ciclo for dentro una `Column` normale**, perché creerebbe tutti i widget contemporaneamente in memoria anche se invisibili.

Usa sempre **`LazyColumn`** (l'equivalente di una RecyclerView o ListView virtualizzata): crea e renderizza solo gli elementi attualmente visibili nella finestra dell'app.

```kotlin
@Composable
fun WorkoutList(
    workouts: List<Workout>,
    onItemClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Spazio di 8dp tra un elemento e l'altro
    ) {
        // ⚠️ IMPORTANTE: Specifica sempre il parametro 'key'!
        // Aiuta Compose a capire quale elemento è stato aggiunto, eliminato o spostato,
        // garantendo animazioni fluide a 60 fps su Desktop.
        items(workouts, key = { workout -> workout.id }) { workout ->
            WorkoutItemRow(
                workout = workout,
                onClick = { onItemClick(workout.id) }
            )
        }
    }
}
```

---

## 8. Riepilogo del Capitolo

- Compose Desktop adotta la programmazione **dichiarativa**: descrivi la grafica come funzione dello stato ($\text{UI} = f(\text{State})$).
- Usa `remember { mutableStateOf(...) }` per mantenere gli stati locali durante la ricomposizione.
- Applica sempre lo **State Hoisting**: rendi i componenti figli privi di stato (`stateless`) passando valori e funzioni lambda (`() -> Unit`).
- Costruisci i layout con `Column`, `Row` e `Box`.
- Per le liste, usa `LazyColumn` specificando sempre la proprietà `key = { it.id }`.

Nel prossimo capitolo vedremo come gestire il **Routing e la Navigazione tra Schermate** su Desktop.
