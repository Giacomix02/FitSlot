package it.univaq.grr.fitslot.ui.screens.CreazioneCorso

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import kotlinx.coroutines.launch

@Preview
@Composable
fun CorsiScreen() {
    val nomeFieldState = rememberTextFieldState()
    val salaFieldState = rememberTextFieldState()
    val fasciaOrariaFieldState = rememberTextFieldState()
    val viewModel: CorsiViewModel = viewModel<CorsiViewModel> { CorsiViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val selectedSala = remember { mutableStateOf(viewModel.saleList.first()) }
    val selectedFasciaOraria = remember { mutableStateOf(viewModel.fasceOrarieList.first()) }
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Creazione corso",
                            Modifier.wrapContentHeight().fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Nome")
                            TextField(
                                state = nomeFieldState,
                                modifier = Modifier.onFocusChanged { if (it.isFocused) viewModel.onFieldInteraction() }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sala")
                            DropDownMenuForSelection(
                                uiState.showSaleDropDown,
                                viewModel::setSaleDropdown,
                                viewModel.saleList,
                                salaFieldState,
                                selectedSala,
                                viewModel::onFieldInteraction
                            ) { sala: Sala ->
                                "${sala.nome}, capienza: ${sala.capienza}"
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fascia Oraria")
                            DropDownMenuForSelection(
                                uiState.showFasceOrarieDropDown,
                                viewModel::setFasceOrarieDropdown,
                                viewModel.fasceOrarieList,
                                fasciaOrariaFieldState,
                                selectedFasciaOraria,
                                viewModel::onFieldInteraction
                            ) { fasciaOraria: FasciaOraria ->
                                "${fasciaOraria.orarioInizio} -> ${fasciaOraria.orarioFine}: ${fasciaOraria.durata} ore"

                            }
                        }
                        Row() {
                            Button(onClick = { viewModel.viewModelScope.launch {
                                viewModel.creaCorso(
                                    nomeFieldState.text.toString(),
                                    selectedSala.value,
                                    selectedFasciaOraria.value
                                )
                            }

                            }) {
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


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun <T> DropDownMenuForSelection(
    showDropDown: Boolean,
    setDropDown: (Boolean) -> Unit, //Funzione per cambiare il booleano
    elementsList: List<T>,
    textFieldState: TextFieldState, //Stato da gestire per cosa è scritto nella selection
    selectedElement: MutableState<T>,
    onInteraction: () -> Unit = {},
    optionText: (element: T) -> String // il testo da mostrare per ogni opzione varia in base all'elemento che diamo
) {
    ExposedDropdownMenuBox(
        expanded = showDropDown, // controlla se il menu è espanso o meno
        onExpandedChange = { show -> setDropDown(show); if (show) onInteraction() },
    ) {
        TextField(
            state = textFieldState,
            readOnly = true,
            modifier = Modifier
                .menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                    true
                )
                .onFocusChanged { if (it.isFocused) onInteraction() }
        )
        ExposedDropdownMenu(
            expanded = showDropDown, // stessa cosa del box
            onDismissRequest = { setDropDown(false) }
        ) {
            elementsList.forEach { element -> // tutti gli elementi dentro
                DropdownMenuItem(
                    text = { Text(optionText(element)) }, // mettiamo il testo impostato in base a quale elemento mostrare
                    onClick = {
                        onInteraction()
                        selectedElement.value = element
                        textFieldState.setTextAndPlaceCursorAtEnd(optionText(element))
                        setDropDown(false)
                    }
                )
            }
        }

    }
}