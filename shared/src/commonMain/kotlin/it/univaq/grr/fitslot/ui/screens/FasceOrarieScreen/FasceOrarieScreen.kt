package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import it.univaq.grr.fitslot.ui.components.FasciaOrariaCard
import it.univaq.grr.fitslot.ui.components.createButton
import it.univaq.grr.fitslot.utils.convertMillisToDate
import it.univaq.grr.fitslot.utils.parseCatching
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")


@Preview
@Composable
fun FasceOrarieScreen(
    viewModel: FasceOrarieViewModel = viewModel { FasceOrarieViewModel() },
    onFabChange: ((@Composable () -> Unit)?) -> Unit = {}
) {

    var showCreateFasciaOrariaDialog by remember { mutableStateOf(false) }

    var showEditFasciaOrariaDialog by remember { mutableStateOf(false) }

    val orarioInizio = rememberTimePickerState()
    val orarioFine = rememberTimePickerState()

    var selectedDate by remember {
        mutableStateOf(LocalDate.now().format(formatter))
    }

    val fasceOrarie by viewModel.fasceOrarie.collectAsStateWithLifecycle()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        onFabChange {
            FloatingActionButton(onClick = {
                viewModel.clearErroreFasciaOraria()
                showCreateFasciaOrariaDialog = true
            }) { Icon(Icons.Default.Add, contentDescription = "Aggiungi fascia oraria") }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp, alignment = Alignment.CenterHorizontally)
        ) {

                DatePickerDocked(selectedDate){selectedDate = it}


            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Fasce Orarie:")
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    fasceOrarie?.filter { fasciaOraria ->
                        fasciaOraria.orarioInizio.toLocalDate().equals(
                                parseCatching(selectedDate, formatter)
                            )

                        }
                    ?.let {list ->
                    if (list.isNotEmpty())
                    items(list.size) { index ->


                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FasciaOrariaCard(list[index])
                            Button(onClick = {
                                viewModel.setSelectedFasciaOraria(orarioInizio, orarioFine, list[index])
                                viewModel.clearErroreFasciaOraria()
                                showEditFasciaOrariaDialog = true}
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Modifica fascia oraria"
                                )
                            }
                            Button(onClick = {
                                viewModel.viewModelScope.launch {
                                    viewModel.deleteFasciaOraria(list[index])
                                }
                            }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Cancella fascia oraria"
                                )
                            }
                        }

                    }
                    }
                }
            }


        }


        if(showCreateFasciaOrariaDialog) {
            FasciaOrariaDialog(Mode.CREATE, onDismissRequest = {
                viewModel.clearErroreFasciaOraria()
                showCreateFasciaOrariaDialog = false
            },
                orarioInizio, orarioFine,
                erroreFasciaOraria = uiState.erroreFasciaOraria) {
                createButton("Crea fascia oraria", enabled = !uiState.showDateNotValidError) {

                    viewModel.viewModelScope.launch {
                        val result = viewModel.creaFasciaOraria(orarioInizio, orarioFine, selectedDate)
                        if (result.isSuccess) showCreateFasciaOrariaDialog = false

                    }
                }

            }
        }
        if (showEditFasciaOrariaDialog) {
            FasciaOrariaDialog(Mode.EDIT,
                onDismissRequest = {
                    viewModel.setDefaultFasciaOrariaState(orarioInizio, orarioFine)
                    viewModel.clearErroreFasciaOraria()
                    showEditFasciaOrariaDialog = false
                },
                orarioInizio, orarioFine,
                erroreFasciaOraria = uiState.erroreFasciaOraria
            ) {
                createButton("Modifica fascia oraria") {

                    viewModel.viewModelScope.launch {
                        val result = viewModel.modificaFasciaOraria(orarioInizio, orarioFine, selectedDate)
                        if (result.isSuccess) showEditFasciaOrariaDialog = false
                    }
                }

            }
        }

    } // chiude Box
}



@Composable
fun DatePickerDocked(selectedDate: String, showDateNotValidError: Boolean, setSelectedDate: (String) -> Unit, setShowDateNotValidError: (Boolean) -> Unit) {
    var showDatePicker: Boolean by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selectedDate) }


    val datePickerState = rememberDatePickerState(initialSelectedDate = parseCatching(selectedDate, formatter))

    // Keep local text in sync with externally-driven selectedDate changes (guarded).
    LaunchedEffect(selectedDate) {
        if (selectedDate != text) {
            text = selectedDate
        }
        setShowDateNotValidError(parseCatching(selectedDate, formatter) == null)
    }

    // Picker selection -> text (guarded to avoid clobbering in-progress typing).
    LaunchedEffect(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis ?: return@LaunchedEffect
        val converted = convertMillisToDate(millis)
        if (converted != text) {
            text = converted
            setSelectedDate(converted)
        }
        setShowDateNotValidError(false)
    }

    Column(horizontalAlignment =  Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentWidth()
    ) {
        OutlinedTextField(
            value = text,
            label = { Text("Giorno") },
            onValueChange = { newText ->
                text = newText
                setSelectedDate(newText)
                val date = parseCatching(newText, formatter)
                if (date != null) {
                    datePickerState.setSelectedDate(date)
                    setShowDateNotValidError(false)
                } else
                    setShowDateNotValidError(true)

            },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            isError = showDateNotValidError,
            supportingText = {
                if (showDateNotValidError) {
                    Text(text = "Data non valida")
                }
            },
            modifier = Modifier
                .requiredWidth(159.dp)
                .height(80.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    setToDatePicker(text, formatter, datePickerState, setShowDateNotValidError)
                    showDatePicker = false
                },
                onGo = { /* do something */},
                onNext = { /* do something */},
                onPrevious = { /* do something */},
                onSearch = { /* do something */},
                onSend = { /* do something */})

        )


        if (showDatePicker) {
            Box(
                modifier = Modifier
                    .requiredWidth(390.dp)
                    .offset(y = 12.dp)
                    .shadow(elevation = 4.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                )
            }
        }
    }
}

private fun setToDatePicker(
    selectedDate: String,
    formatter: DateTimeFormatter,
    datePickerState: DatePickerState,
    setShowDateNotValidError: (Boolean) -> Unit
) {

        val date = parseCatching(selectedDate, formatter){setShowDateNotValidError(true)}
        if (date != null) {
            datePickerState.setSelectedDate(date)
            setShowDateNotValidError(false)
        }
}




