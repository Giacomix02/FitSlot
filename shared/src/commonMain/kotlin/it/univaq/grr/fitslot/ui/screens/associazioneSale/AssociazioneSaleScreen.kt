package it.univaq.grr.fitslot.ui.screens.associazioneSale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.univaq.grr.fitslot.ui.components.DropDownSelector

@Composable
fun AssociazioneSaleScreen(viewModel: AssociazioneSaleViewModel, onIndietro: () -> Unit) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onIndietro, enabled = !viewModel.inCorso) { Text("Torna alla Home") }
        Text("Sale incluse nell'abbonamento", style = MaterialTheme.typography.headlineMedium)
        if (viewModel.inCorso) Text("Caricamento...")
        if (!viewModel.inCorso && (viewModel.abbonamenti.isEmpty() || viewModel.sale.isEmpty())) {
            Text("Crea almeno un abbonamento e una sala nelle rispettive gestioni.")
        }
        DropDownSelector(
            items = viewModel.abbonamenti,
            selectedItem = viewModel.abbonamentoScelto,
            onItemSelected = viewModel::scegliAbbonamento,
            label = "Abbonamento", itemLabel = { it.nome }, enabled = !viewModel.inCorso
        )
        DropDownSelector(
            items = viewModel.sale,
            selectedItem = viewModel.salaScelta,
            onItemSelected = viewModel::scegliSala,
            label = "Sala", itemLabel = { it.nome }, enabled = !viewModel.inCorso
        )
        Button(onClick = viewModel::associa, enabled = !viewModel.inCorso) { Text("Associa sala") }
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
