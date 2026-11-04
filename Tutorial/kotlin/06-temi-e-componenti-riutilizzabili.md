# Capitolo 6: Temi (Material 3) e Componenti Grafici Riutilizzabili

Un'applicazione professionale non ha stili sparsi a caso nel codice: possiede un'identità visiva coerente (palette di colori, dimensioni dei caratteri, arrotondamento degli angoli) e componenti grafici riutilizzabili per evitare la duplicazione del codice.

In questo capitolo esploriamo due cartelle chiave dell'interfaccia utente:
1. **`ui/theme/`**: La configurazione di **Material 3**, della palette di colori e del tema chiaro/scuro.
2. **`ui/components/`**: Come estrarre e costruire componenti grafici modulari e riutilizzabili.

---

## 1. Il Tema dell'Applicazione: `ui/theme/`

In FitSlot utilizziamo il design system **Material 3 (M3)**. Tutta la personalizzazione visiva risiede in tre file essenziali:

### 1.1 La Palette di Colori (`Color.kt`)
Definiamo i codici esadecimali dei colori di FitSlot:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/theme/Color.kt
package it.univaq.grr.fitslot.ui.theme

import androidx.compose.ui.graphics.Color

// Palette FitSlot (Ispirata all'energia e al fitness)
val FitSlotOrange = Color(0xFFFF6D00)
val FitSlotOrangeDark = Color(0xFFC43E00)
val FitSlotTeal = Color(0xFF00BFA5)

// Colori per il tema Chiaro
val LightPrimary = FitSlotOrange
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightOnPrimary = Color(0xFFFFFFFF)

// Colori per il tema Scuro (Dark Mode)
val DarkPrimary = Color(0xFFFF9E40)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnPrimary = Color(0xFF000000)
```

---

### 1.2 La Tipografia (`Type.kt`)
Definiamo la scala dei caratteri (titoli, testi di paragrafo, bottoni):

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/theme/Type.kt
package it.univaq.grr.fitslot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FitSlotTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)
```

---

### 1.3 Il Composable del Tema (`Theme.kt`)
Assembliamo i colori e la tipografia all'interno di una funzione Composable che avvolge tutta l'app:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/theme/Theme.kt
package it.univaq.grr.fitslot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary
)

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary
)

@Composable
fun FitSlotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Rileva automaticamente se il PC è in Dark Mode
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = FitSlotTypography,
        content = content
    )
}
```

Da questo momento in poi, qualsiasi componente dell'app può usare:
- `MaterialTheme.colorScheme.primary` per avere il colore arancione principale.
- `MaterialTheme.typography.titleMedium` per avere lo stile di testo corretto.

---

## 2. Componenti Grafici Riutilizzabili: `ui/components/`

Non copiare e incollare mai codice grafico identico in più schermate! Se hai un bottone con uno stile specifico o un dialogo di conferma, estrailo nella cartella `ui/components/`.

Ecco 3 esempi concreti realizzati per FitSlot:

### 2.1 Bottone Personalizzato con Stato di Caricamento (`FitSlotButton.kt`)

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/components/FitSlotButton.kt
package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FitSlotButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp) // Angoli arrotondati standard di FitSlot
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
```

---

### 2.2 Dialog di Conferma Eliminazione (`ConfirmDeleteDialog.kt`)

Spesso in FitSlot dobbiamo chiedere: *"Sei sicuro di voler eliminare questo allenamento?"*. 
Creiamo una finestra di dialogo universale:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/components/ConfirmDeleteDialog.kt
package it.univaq.grr.fitslot.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDeleteDialog(
    isOpen: Boolean,
    title: String = "Conferma eliminazione",
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }
}
```

---

### 2.3 Vista di Stato Vuoto (`EmptyStateView.kt`)

Quando una lista non ha dati (es. zero allenamenti o zero slot), non lasciare uno schermo bianco desolato. Mostra un messaggio amichevole:

```kotlin
// Percorso: shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/components/EmptyStateView.kt
package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmptyStateView(
    iconEmoji: String = "🏋️‍♂️",
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = iconEmoji, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

## 3. Le 3 Regole per Scrivere Componenti Riutilizzabili

1. **Accetta sempre un parametro `modifier: Modifier = Modifier`:**
   Permette a chi usa il componente di decidere il margine esterno, la larghezza (`fillMaxWidth()`) o il peso senza dover modificare il componente interno.
2. **Usa le lambda per gli eventi (`onClick: () -> Unit`):**
   Non inserire mai logica di business dentro un componente riutilizzabile: deve solo notificare il click verso l'esterno.
3. **Usa i colori del tema (`MaterialTheme.colorScheme`):**
   Non usare colori hardcodati come `Color.Black` o `Color.White`, altrimenti quando l'utente attiva la Dark Mode il componente diventerà illeggibile!

---

## 4. Riepilogo del Capitolo

- La cartella `ui/theme/` definisce l'identità visiva dell'applicazione: colori, font e supporto alla Dark Mode tramite `FitSlotTheme`.
- La cartella `ui/components/` contiene bottoni, card, finestre di dialogo e viste vuote modulari e riutilizzabili.
- Creare componenti stateless con lambda e `Modifier` rende il codice dell'interfaccia elegante, manutenibile e scalabile.

Nel prossimo capitolo scopriremo le **Best Practices per Mantenere Tutto Separato e Pulito** ed evitare il degrado del codice nel tempo.
