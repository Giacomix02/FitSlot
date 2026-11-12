package it.univaq.grr.fitslot.ui.screens.CreazioneCorso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.ui.components.CustomTextField
import it.univaq.grr.fitslot.ui.components.DropDownSelector
import kotlinx.coroutines.launch

@Preview
@Composable
fun CorsiScreen() {
    var nomeCorso by remember { mutableStateOf("") }
    val viewModel: CorsiViewModel = viewModel<CorsiViewModel> { CorsiViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val saleList by viewModel.saleList.collectAsStateWithLifecycle()

    var selectedSala by remember { mutableStateOf<Sala?>(null) }
    var selectedFasciaOraria by remember { mutableStateOf<FasciaOraria?>(null) }

    fun resetCampi() {
        nomeCorso = ""
        selectedSala = null
        selectedFasciaOraria = null
    }

    // Opzioni mostrate nei menu: se è già stata scelta una fascia oraria
    // mostriamo solo le sale libere e viceversa.
    var saleSelezionabili by remember { mutableStateOf<List<Sala>>(emptyList()) }
    var fasceSelezionabili by remember { mutableStateOf(viewModel.fasceOrarieList) }

    LaunchedEffect(saleList, selectedFasciaOraria, uiState.showCreazioneCorsoEseguita) {
        saleSelezionabili = selectedFasciaOraria
            ?.let { fascia -> viewModel.saleLibereInFascia(fascia) }
            ?: saleList
    }
    LaunchedEffect(selectedSala, uiState.showCreazioneCorsoEseguita) {
        fasceSelezionabili = selectedSala
            ?.let { sala -> viewModel.fasceLibereInSala(sala) }
            ?: viewModel.fasceOrarieList
    }

    FloatingActionButton(
        modifier = Modifier.width(16.dp).height(16.dp).wrapContentSize(),
        onClick = { viewModel.showDialog() },
    )
    { Icon(Icons.Default.Add, contentDescription = "Aggiungi corso") }
    if (uiState.showDialog) {
        Dialog(
            onDismissRequest = { viewModel.hideDialog() },
            content = {
                Card {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Creazione corso",
                            Modifier.wrapContentHeight().fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        CustomTextField(
                            value = nomeCorso,
                            onValueChange = {
                                nomeCorso = it
                                viewModel.onFieldInteraction()
                            },
                            label = "Nome",
                            placeholder = "Inserisci il nome del corso",
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropDownSelector(
                            items = saleSelezionabili,
                            selectedItem = selectedSala,
                            onItemSelected = { sala ->
                                selectedSala = sala
                                viewModel.onFieldInteraction()
                            },
                            label = "Sala",
                            itemLabel = { sala -> "${sala.nome}, capienza: ${sala.capienza}" },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (selectedFasciaOraria != null && saleSelezionabili.isEmpty()) {
                            Text(
                                "Nessuna sala libera in questa fascia oraria",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        DropDownSelector(
                            items = fasceSelezionabili,
                            selectedItem = selectedFasciaOraria,
                            onItemSelected = { fasciaOraria ->
                                selectedFasciaOraria = fasciaOraria
                                viewModel.onFieldInteraction()
                            },
                            label = "Fascia Oraria",
                            itemLabel = { fasciaOraria ->
                                "${fasciaOraria.orarioInizio} -> ${fasciaOraria.orarioFine}: ${fasciaOraria.durata} ore"
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (selectedSala != null && fasceSelezionabili.isEmpty()) {
                            Text(
                                "Nessuna fascia oraria libera per questa sala",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val salaDaCreare = selectedSala ?: saleSelezionabili.firstOrNull()
                            val fasciaDaCreare = selectedFasciaOraria ?: fasceSelezionabili.firstOrNull()
                            Button(
                                enabled = salaDaCreare != null && fasciaDaCreare != null,
                                onClick = {
                                    val sala = salaDaCreare ?: return@Button
                                    val fascia = fasciaDaCreare ?: return@Button
                                    viewModel.viewModelScope.launch {
                                        if (viewModel.creaCorso(nomeCorso, sala, fascia)) {
                                            resetCampi()
                                        }
                                    }
                                }
                            ) {
                                Row {
                                    Text("Crea corso")
                                    Icon(Icons.Default.Add, contentDescription = "aggiungi corso")
                                }
                            }
                            if (uiState.showCreazioneCorsoEseguita) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Aggiunta eseguita con successo",
                                    tint = MaterialTheme.colorScheme.surfaceTint,
                                )
                            }
                        }
                        uiState.erroreCreazioneCorso?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        )
    }

}
