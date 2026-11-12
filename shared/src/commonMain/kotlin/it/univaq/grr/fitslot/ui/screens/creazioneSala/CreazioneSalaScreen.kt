package it.univaq.grr.fitslot.ui.screens.creazioneSala
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.univaq.grr.fitslot.domain.models.Sala
/** Modulo e lista per la gestione delle sale. */
@Composable
fun CreazioneSalaScreen(
    stato: CreazioneSalaUiState, onNomeModificato: (String) -> Unit,
    onCapienzaModificata: (String) -> Unit, onSalva: () -> Unit,
    onModifica: (Sala) -> Unit, onAnnullaModifica: () -> Unit,
    onElimina: (Sala) -> Unit, onConfermaEliminazione: () -> Unit,
    onAnnullaEliminazione: () -> Unit
) {
    val abilitato = !stato.salvataggioInCorso
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Gestione sale", style = MaterialTheme.typography.headlineMedium) }
        item { Text(if (stato.idModifica == null) "Nuova sala" else "Modifica sala", style = MaterialTheme.typography.titleLarge) }
        item { OutlinedTextField(stato.nome, onNomeModificato, label = { Text("Nome") }, singleLine = true, enabled = abilitato, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(stato.capienza, onCapienzaModificata, label = { Text("Capienza") }, singleLine = true, enabled = abilitato, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSalva, enabled = abilitato) { Text(if (!abilitato) "Operazione in corso..." else if (stato.idModifica == null) "Crea sala" else "Salva modifiche") }
                if (stato.idModifica != null) TextButton(onClick = onAnnullaModifica, enabled = abilitato) { Text("Annulla modifica") }
            }
        }
        item {
            stato.errore?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            stato.conferma?.let { Text(it) }
        }
        item {
            Text("Sale salvate", style = MaterialTheme.typography.titleLarge)
            if (stato.caricamento) Text("Caricamento...")
            else if (stato.elenco.isEmpty()) Text("Non ci sono sale. Crea la prima compilando i campi.")
        }
        items(stato.elenco, key = { it.id }) { sala ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(sala.nome, style = MaterialTheme.typography.titleMedium)
                    Text("Capienza: " + sala.capienza)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { onModifica(sala) }, enabled = abilitato) { Text("Modifica") }
                        TextButton(onClick = { onElimina(sala) }, enabled = abilitato) { Text("Elimina") }
                    }
                }
            }
        }
    }
    stato.daEliminare?.let { sala ->
        AlertDialog(
            onDismissRequest = { if (abilitato) onAnnullaEliminazione() },
            title = { Text("Elimina sala") },
            text = { Text("Vuoi eliminare “" + sala.nome + "”? Se è assegnata a un corso, l'eliminazione sarà bloccata.") },
            confirmButton = { TextButton(onClick = onConfermaEliminazione, enabled = abilitato) { Text("Elimina") } },
            dismissButton = { TextButton(onClick = onAnnullaEliminazione, enabled = abilitato) { Text("Annulla") } }
        )
    }
}
