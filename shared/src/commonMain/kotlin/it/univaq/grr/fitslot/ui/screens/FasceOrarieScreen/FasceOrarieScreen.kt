package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.setSelectedDate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import it.univaq.grr.fitslot.utils.parseCatching
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone


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

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

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
                erroreFasciaOraria = uiState.value.erroreFasciaOraria) {
                createButton("Crea fascia oraria") {

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
                erroreFasciaOraria = uiState.value.erroreFasciaOraria
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
fun DatePickerDocked(selectedDate: String, setSelectedDate: (String) -> Unit) {
    var showDatePicker: Boolean by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selectedDate) }
    var showDateNotValidError by remember { mutableStateOf(parseCatching(selectedDate, formatter) == null) }

    val datePickerState = rememberDatePickerState(initialSelectedDate = parseCatching(selectedDate, formatter))

    // Keep local text in sync with externally-driven selectedDate changes (guarded).
    LaunchedEffect(selectedDate) {
        if (selectedDate != text) {
            text = selectedDate
        }
        showDateNotValidError = parseCatching(selectedDate, formatter) == null
    }

    // Picker selection -> text (guarded to avoid clobbering in-progress typing).
    LaunchedEffect(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis ?: return@LaunchedEffect
        val converted = convertMillisToDate(millis)
        if (converted != text) {
            text = converted
            setSelectedDate(converted)
        }
        showDateNotValidError = false
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
                    showDateNotValidError = false
                } else {
                    showDateNotValidError = true
                }
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
                    setToDatePicker(text, formatter, datePickerState) { showDateNotValidError = it }
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

private fun ColumnScope.setToDatePicker(
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


fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}


