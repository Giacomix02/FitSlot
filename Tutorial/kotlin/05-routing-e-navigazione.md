# Capitolo 5: Routing e Navigazione tra Schermate (Compose Desktop)

Nel mondo dello sviluppo Android tradizionale, la navigazione tra schermate è spesso complessa: frammenti, Activity, grafi XML e librerie con percorsi in stile URL (`"workout/{id}"`). 

Su **Desktop**, l'applicazione ha esigenze diverse e molto più pulite: una finestra principale con una barra laterale (Sidebar) o un pulsante "Indietro", in cui vogliamo passare da una schermata all'altra in modo **fluido, immediato e al 100% type-safe**.

In questo capitolo vediamo come implementare il sistema di navigazione per FitSlot nella cartella `ui/navigator/`.

---

## 1. Il Concetto: Destinazioni Type-Safe con `sealed interface`

Il modo più robusto ed elegante in Kotlin per definire le possibili schermate dell'applicazione è una **`sealed interface`**:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/navigator/Screen.kt
package it.univaq.grr.fitslot.ui.navigator

sealed interface Screen {
    /**
     * Schermata principale: lista di tutti i workout
     */
    data object WorkoutList : Screen

    /**
     * Schermata di dettaglio di un singolo workout:
     * Nota la type-safety: passiamo direttamente il Long 'workoutId',
     * senza dover convertire stringhe o fare parsing di URL!
     */
    data class WorkoutDetail(val workoutId: Long) : Screen

    /**
     * Schermata del calendario degli slot orari
     */
    data object SlotCalendar : Screen

    /**
     * Schermata delle impostazioni dell'app
     */
    data object Settings : Screen
}
```

### Vantaggi di questo approccio:
1. **Zero errori a runtime:** Se una schermata richiede un ID (`workoutId: Long`), è impossibile dimenticarsi di passarlo: il codice non compilerebbe!
2. **Nessun parsing:** Non dobbiamo convertire numeri in stringhe e viceversa.
3. **Navigazione leggibile:** Chiamare `navigator.navigateTo(Screen.WorkoutDetail(42))` è chiaro ed autoesplicativo.

---

## 2. Creare il `Navigator` (Gestore dello Stack di Schermate)

Il `Navigator` è una semplice classe che mantiene la cronologia delle schermate (chiamata **Backstack**):

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/navigator/Navigator.kt
package it.univaq.grr.fitslot.ui.navigator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Navigator(initialScreen: Screen = Screen.WorkoutList) {

    // Lo stack di navigazione (per gestire il tasto "Indietro")
    private val backStack = mutableStateListOf<Screen>(initialScreen)

    // La schermata attualmente visibile sullo schermo
    val currentScreen: Screen
        get() = backStack.lastOrNull() ?: Screen.WorkoutList

    // Indica se c'è almeno una schermata precedente a cui tornare
    val canGoBack: Boolean
        get() = backStack.size > 1

    /**
     * Va a una nuova schermata aggiungendola in cima allo stack.
     */
    fun navigateTo(screen: Screen) {
        backStack.add(screen)
    }

    /**
     * Torna alla schermata precedente rimuovendo quella attuale.
     * Restituisce true se è tornato indietro, false se eravamo già alla prima schermata.
     */
    fun navigateBack(): Boolean {
        return if (canGoBack) {
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    /**
     * Sostituisce la schermata attuale senza aggiungerla allo stack (utile per i menu laterali).
     */
    fun replaceRoot(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
    }
}
```

---

## 3. Il Routing in `App.kt`: Il Router Dichiarativo

Ora possiamo collegare il nostro `Navigator` al composable principale dell'applicazione (`App.kt`). 

Grazie all'uso del `when` esaustivo su `sealed interface`, il router è pulito, leggibile e sicuro:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/App.kt
package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import it.univaq.grr.fitslot.ui.navigator.Navigator
import it.univaq.grr.fitslot.ui.navigator.Screen
import it.univaq.grr.fitslot.ui.screens.workout.WorkoutDetailScreen
import it.univaq.grr.fitslot.ui.screens.workout.WorkoutListScreen
import it.univaq.grr.fitslot.ui.theme.FitSlotTheme

@Composable
fun App() {
    // Creiamo e ricordiamo l'istanza del Navigator per tutta la durata dell'app
    val navigator = remember { Navigator(Screen.WorkoutList) }

    FitSlotTheme {
        Scaffold(
            topBar = {
                FitSlotTopBar(
                    title = when (navigator.currentScreen) {
                        is Screen.WorkoutList -> "FitSlot - I Miei Allenamenti"
                        is Screen.WorkoutDetail -> "Dettaglio Allenamento"
                        is Screen.SlotCalendar -> "Calendario Slot"
                        is Screen.Settings -> "Impostazioni"
                    },
                    canGoBack = navigator.canGoBack,
                    onBackClick = { navigator.navigateBack() }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // 🧭 IL ROUTER: Disegna la schermata corrente
                when (val current = navigator.currentScreen) {
                    is Screen.WorkoutList -> {
                        WorkoutListScreen(
                            onNavigateToDetail = { workoutId ->
                                navigator.navigateTo(Screen.WorkoutDetail(workoutId))
                            }
                        )
                    }
                    is Screen.WorkoutDetail -> {
                        WorkoutDetailScreen(
                            workoutId = current.workoutId,
                            onBackClick = { navigator.navigateBack() }
                        )
                    }
                    is Screen.SlotCalendar -> {
                        // SlotCalendarScreen(...)
                    }
                    is Screen.Settings -> {
                        // SettingsScreen(...)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FitSlotTopBar(
    title: String,
    canGoBack: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = onBackClick) {
                    Text("◀") // O un'icona freccia indietro
                }
            }
        }
    )
}
```

---

## 4. Best Practice: Disaccoppiare le Schermate dal Navigator

> [!IMPORTANT]
> **Non passare mai l'oggetto `Navigator` all'interno delle schermate!**
> 
> Se una schermata fa:
> ```kotlin
> // ❌ MALA PRATICA: La schermata è accoppiata al Navigator
> @Composable
> fun WorkoutListScreen(navigator: Navigator) {
>     Button(onClick = { navigator.navigateTo(Screen.Detail) }) { ... }
> }
> ```
> Diventa impossibile visualizzare l'anteprima (`@Preview`) o testare la schermata da sola.
> 
> **Fai sempre State Hoisting con una lambda:**
> ```kotlin
> // ✅ BUONA PRATICA: La schermata emette solo un evento lambda
> @Composable
> fun WorkoutListScreen(onNavigateToDetail: (Long) -> Unit) {
>     Button(onClick = { onNavigateToDetail(workout.id) }) { ... }
> }
> ```
> In questo modo la schermata comunica solo cosa vorrebbe fare, e sarà il Router in `App.kt` a decidere dove andare!

---

## 5. Riepilogo del Capitolo

- Su Desktop usiamo una **`sealed interface Screen`** per definire tutte le destinazioni in modo type-safe.
- La classe `Navigator` gestisce lo stack di navigazione (`backStack`), consentendo di andare avanti e tornare indietro.
- In `App.kt`, un semplice blocco `when (navigator.currentScreen)` seleziona e renderizza la schermata attiva.
- Le schermate non devono dipendere dal `Navigator`: passano solo callback lambda (`(Long) -> Unit`) al chiamante.

Nel prossimo capitolo scopriremo come organizzare **Temi Personalizzati e Componenti Grafici Riutilizzabili**.
