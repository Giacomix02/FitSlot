package it.univaq.grr.fitslot.ui.screens.associazioneSale

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
fun AssociazioneSaleDialog(viewModel: AssociazioneSaleViewModel, onChiudi: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!viewModel.inCorso) onChiudi() },
        title = { Text("Sale incluse: ${viewModel.abbonamentoScelto?.nome.orEmpty()}") },
        confirmButton = { TextButton(onClick = onChiudi, enabled = !viewModel.inCorso) { Text("Chiudi") } },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (viewModel.inCorso) Text("Caricamento...")
                if (!viewModel.inCorso && viewModel.sale.isEmpty()) Text("Crea prima una sala nella gestione sale.")
                SelezioneMultipla("Sale", viewModel.sale.map { it.id to it.nome },
                    viewModel.saleSelezionate, viewModel.saleIncluse.map { it.id }.toSet(),
                    !viewModel.inCorso, viewModel::scegliSala)
                Button(onClick = viewModel::associa, enabled = !viewModel.inCorso && viewModel.saleSelezionate.isNotEmpty()) { Text("Associa sale") }
                Text(viewModel.messaggio)
                if (viewModel.abbonamentoScelto != null && !viewModel.inCorso) {
                    Text("Riepilogo delle sale incluse", style = MaterialTheme.typography.titleLarge)
                    if (viewModel.saleIncluse.isEmpty()) Text("Nessuna sala inclusa.")
                    for (sala in viewModel.saleIncluse) {
                        Text(sala.nome)
                    }
                }
            }
        }
    )
}
