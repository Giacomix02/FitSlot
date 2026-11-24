package it.univaq.grr.fitslot.ui.screens.CorsiScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import it.univaq.grr.fitslot.ui.components.createButton
import kotlinx.coroutines.launch

@Preview
@Composable
fun CorsiScreen() {
    var nomeCorso by remember { mutableStateOf("") }
    val viewModel: CorsiViewModel = viewModel<CorsiViewModel> { CorsiViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val saleList by viewModel.saleList.collectAsStateWithLifecycle()
    val fasceList by viewModel.fasceOrarie.collectAsStateWithLifecycle()

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
    var fasceSelezionabili by remember { mutableStateOf<List<FasciaOraria>>(emptyList()) }

    LaunchedEffect(saleList, selectedFasciaOraria, uiState.showCreazioneCorsoEseguita) {
        saleSelezionabili = selectedFasciaOraria
            ?.let { fascia -> viewModel.saleLibereInFascia(fascia) }
            ?: saleList
    }
    LaunchedEffect(selectedSala, uiState.showCreazioneCorsoEseguita) {
        fasceSelezionabili = selectedSala
            ?.let { sala -> viewModel.fasceLibereInSala(sala) }
            ?: fasceList
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
                            createButton("Crea corso", enabled = salaDaCreare != null && fasciaDaCreare != null) {
                                val sala = salaDaCreare ?: return@createButton
                                val fascia = fasciaDaCreare ?: return@createButton
                                viewModel.viewModelScope.launch {

                                    if (viewModel.creaCorso(nomeCorso, sala, fascia)) {
                                        resetCampi()
                                    }
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

