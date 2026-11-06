package it.univaq.grr.fitslot.ui.screens.creazionesala

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Mostra i campi per creare una sala e l'esito del salvataggio.
 * Le azioni dell'utente vengono inoltrate tramite le funzioni ricevute.
 */
@Composable
fun CreazioneSalaScreen(
    stato: CreazioneSalaUiState,
    onNomeModificato: (String) -> Unit,
    onCapienzaModificata: (String) -> Unit,
    onSalva: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Creazione sala",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = stato.nome,
            onValueChange = onNomeModificato,
            label = { Text("Nome") },
            singleLine = true,
            enabled = !stato.salvataggioInCorso,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stato.capienza,
            onValueChange = onCapienzaModificata,
            label = { Text("Capienza") },
            singleLine = true,
            enabled = !stato.salvataggioInCorso,
            modifier = Modifier.fillMaxWidth()
        )

        // Impedisce nuovi invii durante il salvataggio o dopo il successo.
        Button(
            onClick = onSalva,
            enabled = !stato.salvataggioInCorso && !stato.salaCreata
        ) {
            Text(
                text = if (stato.salvataggioInCorso) {
                    "Salvataggio..."
                } else {
                    "Salva"
                }
            )
        }

        // Il messaggio compare soltanto quando è presente un errore.
        stato.errore?.let { messaggio ->
            Text(
                text = messaggio,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (stato.salaCreata) {
            Text("Sala creata correttamente.")
        }
    }
}