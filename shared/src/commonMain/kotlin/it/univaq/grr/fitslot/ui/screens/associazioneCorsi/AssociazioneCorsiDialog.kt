package it.univaq.grr.fitslot.ui.screens.associazioneCorsi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.univaq.grr.fitslot.ui.components.SelezioneMultipla

@Composable
fun AssociazioneCorsiDialog(viewModel: AssociazioneCorsiViewModel, onChiudi: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!viewModel.inCorso) onChiudi() },
        title = { Text("Corsi inclusi: ${viewModel.abbonamentoScelto?.nome.orEmpty()}") },
        confirmButton = { TextButton(onClick = onChiudi, enabled = !viewModel.inCorso) { Text("Chiudi") } },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (viewModel.inCorso) Text("Caricamento...")
                if (!viewModel.inCorso && viewModel.corsi.isEmpty()) Text("Crea prima un corso nella gestione corsi.")
                SelezioneMultipla("Corsi", viewModel.corsi.map { it.id to it.name },
                    viewModel.corsiSelezionati, viewModel.corsiInclusi.map { it.id }.toSet(),
                    !viewModel.inCorso, viewModel::scegliCorso)
                Button(onClick = viewModel::associa, enabled = !viewModel.inCorso && viewModel.corsiSelezionati.isNotEmpty()) { Text("Associa corsi") }
                Text(viewModel.messaggio)
                if (viewModel.abbonamentoScelto != null && !viewModel.inCorso) {
                    Text("Riepilogo dei corsi inclusi", style = MaterialTheme.typography.titleLarge)
                    if (viewModel.corsiInclusi.isEmpty()) Text("Nessun corso incluso.")
                    for (corso in viewModel.corsiInclusi) {
                        Text(corso.name)
                    }
                }
            }
        }
    )
}
