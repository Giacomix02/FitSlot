# Capitolo 6: Clean Architecture, Repository e Integrazione UI Compose

Nei capitoli precedenti abbiamo visto come salvare e leggere dati da SQLite tramite Room 3. Ma come colleghiamo questo meccanismo all'interfaccia grafica creata con **Compose Multiplatform (Desktop)**?

In questo capitolo esploriamo l'architettura a strati (**Clean Architecture**) del progetto FitSlot, mostrando perché l'interfaccia grafica non deve **mai** toccare direttamente le classi di Room e come implementare un flusso reattivo completo e scalabile.

---

## 1. L'Errore del Principiante: Usare le Entity nella UI

Quando si inizia a usare Room, la tentazione più comune è questa:
```kotlin
// ❌ PESSIMA PRATICA: La UI usa direttamente l'Entity del database
@Composable
fun WorkoutCard(workout: WorkoutEntity) {
    Text(workout.name)
}
```

Perché questo approccio è considerato un grave errore di progettazione?
1. **Accoppiamento stretto:** Se un domani cambi il database (ad esempio passi da SQLite a un'API REST cloud o cambi il nome di una colonna), dovrai riscrivere anche tutte le schermate grafiche dell'applicazione.
2. **Campi tecnici non pertinenti:** Le `Entity` contengono dettagli tipici dei database (chiavi primarie autogenerate, timestamp grezzi in millisecondi, vincoli di foreign key) che non hanno alcun senso logico per la visualizzazione dell'utente.
3. **Impossibilità di testare la UI in isolamento:** Per provare una schermata saresti costretto a inizializzare l'intero motore di database Room.

---

## 2. L'Architettura a Tre Strati di FitSlot

Per risolvere questo problema separiamo nettamente le responsabilità in tre livelli:

```
┌────────────────────────────────────────────────────────┐
│                        UI                              │
│  Compose Desktop Screens & ViewModels                  │
│  (Cosa vede e tocca l'utente)                          │
└───────────────────────────┬────────────────────────────┘
                            │ conosce solo il Domain
                            ▼
┌────────────────────────────────────────────────────────┐
│                      DOMAIN                            │
│  Modelli Puri (Workout) & Interfacce Repository        │
│  (Kotlin puro: ZERO dipendenze da Room o Compose)      │
└───────────────────────────▲────────────────────────────┘
                            │ implementa il Domain
                            │
┌───────────────────────────┴────────────────────────────┐
│                       DATA                             │
│  Room 3: Entity, DAO, SQLite, Database & Mapper        │
│  (Come e dove vengono salvati fisicamente i dati)      │
└────────────────────────────────────────────────────────┘
```

---

## 3. Passo 1: Il Modello Puro del Domain

Nel layer `domain` definiamo il concetto di `Workout` come deve essere compreso dalla nostra applicazione, senza annotazioni di database:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/domain/models/Workout.kt
package it.univaq.grr.fitslot.domain.models

/**
 * Modello di dominio puro. Non contiene @Entity, né @ColumnInfo.
 */
data class Workout(
    val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val notes: String? = null
)
```

Nel `domain` definiamo anche l'interfaccia del Repository:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/domain/repository/WorkoutRepository.kt
package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeAllWorkouts(): Flow<List<Workout>>
    suspend fun getWorkoutById(id: Long): Workout?
    suspend fun addWorkout(workout: Workout): Long
    suspend fun deleteWorkout(id: Long)
}
```

---

## 4. Passo 2: I Mapper (Traduttori di Dati)

Nel layer `data` creiamo delle semplici funzioni di estensione per tradurre le classi del database (`WorkoutEntity`) nei modelli del dominio (`Workout`) e viceversa:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/data/mapper/WorkoutMapper.kt
package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.WorkoutEntity
import it.univaq.grr.fitslot.domain.models.Workout

// Da Entity del DB -> a Modello del Dominio
fun WorkoutEntity.toDomain(): Workout = Workout(
    id = id,
    name = name,
    durationMinutes = durationMinutes,
    notes = notes
)

// Da Modello del Dominio -> a Entity del DB
fun Workout.toEntity(createdAt: Long = System.currentTimeMillis()): WorkoutEntity = WorkoutEntity(
    id = id,
    name = name,
    durationMinutes = durationMinutes,
    notes = notes,
    createdAt = createdAt
)

// Traduzione per intere liste
fun List<WorkoutEntity>.toDomain(): List<Workout> = map { it.toDomain() }
```

---

## 5. Passo 3: Implementazione del Repository

Sempre nel layer `data`, creiamo la classe che riceve il DAO di Room, esegue le query e mappa i dati verso il dominio:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/data/repository/WorkoutRepositoryImpl.kt
package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.WorkoutDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.Workout
import it.univaq.grr.fitslot.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun observeAllWorkouts(): Flow<List<Workout>> {
        // Prendiamo il Flow<List<WorkoutEntity>> di Room e con .map {} 
        // trasformiamo ogni emissione in una List<Workout> del dominio!
        return workoutDao.observeAll().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getWorkoutById(id: Long): Workout? {
        return workoutDao.getById(id)?.toDomain()
    }

    override suspend fun addWorkout(workout: Workout): Long {
        return workoutDao.insert(workout.toEntity())
    }

    override suspend fun deleteWorkout(id: Long) {
        workoutDao.deleteById(id)
    }
}
```

---

## 6. Passo 4: Il ViewModel e la Trasformazione in `StateFlow`

Il ViewModel gestisce lo stato dell'interfaccia utente. Prende il `Flow` del repository e lo converte in uno **`StateFlow`**:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/screens/workout/WorkoutListViewModel.kt
package it.univaq.grr.fitslot.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.Workout
import it.univaq.grr.fitslot.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    /**
     * Lo StateFlow che rappresenta lo stato della lista per lo schermo.
     * Si aggiornerà da solo ogni volta che il database subisce variazioni!
     */
    val workouts: StateFlow<List<Workout>> = repository.observeAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = emptyList()
        )

    /**
     * Aggiunge un workout lanciando una coroutine sul viewModelScope.
     */
    fun createWorkout(name: String, durationMinutes: Int, notes: String? = null) {
        viewModelScope.launch {
            val newWorkout = Workout(
                name = name,
                durationMinutes = durationMinutes,
                notes = notes
            )
            repository.addWorkout(newWorkout)
        }
    }

    /**
     * Cancella un workout per ID.
     */
    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }
}
```

### Perché usiamo `SharingStarted.WhileSubscribed(5_000)`?
1. **Risparmio risorse:** Se l'utente chiude la schermata o minimizza l'app, non ha senso che Room continui a interrogare il database in background. `WhileSubscribed` interrompe l'ascolto quando nessuno schermo sta guardando i dati.
2. **Il timeout di 5000 millisecondi (5 secondi):** Evita di distruggere e ricreare la connessione al database se l'utente compie una rapida transizione di finestra o rotazione dello schermo.

---

## 7. Passo 5: L'Interfaccia Utente Compose Desktop

Infine, colleghiamo il nostro ViewModel al codice Compose Desktop:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/screens/workout/WorkoutListScreen.kt
package it.univaq.grr.fitslot.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.univaq.grr.fitslot.domain.models.Workout

@Composable
fun WorkoutListScreen(
    viewModel: WorkoutListViewModel,
    modifier: Modifier = Modifier
) {
    // 1. Raccogliamo lo StateFlow del ViewModel convertendolo in uno State di Compose
    val workoutList by viewModel.workouts.collectAsState()
    
    var newWorkoutName by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("45") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Allenamenti FitSlot", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Modulo per inserire un nuovo elemento
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newWorkoutName,
                onValueChange = { newWorkoutName = it },
                label = { Text("Nome Allenamento") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it },
                label = { Text("Minuti") },
                modifier = Modifier.width(100.dp)
            )

            Button(
                onClick = {
                    val minutes = durationText.toIntOrNull() ?: 30
                    if (newWorkoutName.isNotBlank()) {
                        viewModel.createWorkout(newWorkoutName, minutes)
                        newWorkoutName = "" // Reset del campo di testo
                    }
                }
            ) {
                Text("Aggiungi")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Lista visualizzata: si aggiorna in tempo reale senza ricaricare nulla!
        if (workoutList.isEmpty()) {
            Text("Nessun allenamento presente. Inseriscine uno sopra!", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(workoutList, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onDeleteClick = { viewModel.deleteWorkout(workout.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(workout.name, style = MaterialTheme.typography.titleMedium)
                Text("Durata: ${workout.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDeleteClick) {
                Text("🗑️")
            }
        }
    }
}
```

---

## 8. Come Funziona la Magia della Reattività

Segui cosa accade quando l'utente preme il pulsante **"Aggiungi"**:
1. Il pulsante chiama `viewModel.createWorkout("Crossfit", 60)`.
2. Il ViewModel avvia una coroutine ed esegue `repository.addWorkout(...)`.
3. Il Repository mappa il dato in `WorkoutEntity` e invoca `workoutDao.insert(...)`.
4. Room esegue l'istruzione SQL `INSERT INTO workout ...` nel file `.db`.
5. SQLite conferma la scrittura. L'Invalidation Tracker interno di Room avverte il `Flow`.
6. Il `Flow` di Room riesegue la query in background ed emette la nuova lista di entity.
7. Il Repository mappa automaticamente le entity in modelli `Workout`.
8. Lo `StateFlow` del ViewModel riceve la nuova lista e aggiorna il proprio valore.
9. Compose rileva il cambio di stato tramite `collectAsState()` e ridisegna solo la card appena aggiunta.

**Risultato:** Non hai dovuto scrivere nessuna riga di codice tipo `reloadList()` o `fetchDataAgain()`. Il sistema è completamente reattivo e sincronizzato!

---

## 9. Riepilogo del Capitolo

- La UI non deve mai utilizzare le `@Entity` di Room: usiamo i modelli puliti del package `domain`.
- Usiamo i **Mapper** per convertire in modo esplicito tra modelli di database e modelli di dominio.
- Il **Repository** incapsula il DAO e fornisce `Flow` puliti verso l'applicazione.
- Nel **ViewModel**, trasformiamo i `Flow` in `StateFlow` tramite `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)`.
- La UI Compose osserva lo `StateFlow` con `collectAsState()`, ottenendo aggiornamenti istantanei e automatici dello schermo.

Nel prossimo capitolo affronteremo uno dei temi più critici per un'applicazione professionale: **Transazioni, Versioning e Migrazioni del Database**.
