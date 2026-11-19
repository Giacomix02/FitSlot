package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.ui.components.FasciaOrariaCard
import it.univaq.grr.fitslot.ui.components.createButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


val fasceOrarie = listOf(
    FasciaOraria(1, LocalDateTime.parse("2027-06-01T15:30"), LocalDateTime.parse("2027-06-01T17:30")),
    FasciaOraria(2, LocalDateTime.parse("2027-06-01T18:30"), LocalDateTime.parse("2027-06-01T19:30")),
    FasciaOraria(3, LocalDateTime.parse("2027-06-02T12:30"), LocalDateTime.parse("2027-06-02T15:00"))
)


val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")


@Preview
@Composable
fun FasceOrarieScreen(viewModel: FasceOrarieViewModel = viewModel { FasceOrarieViewModel() }) {
    val showCreateFasciaOrariaDialog = remember { mutableStateOf(false) }
    val showEditFasciaOrariaDialog = remember { mutableStateOf(false) }

    val orarioInizio = rememberTimePickerState()
    val orarioFine = rememberTimePickerState()

    val selectedDate = remember {
        mutableStateOf(LocalDate.now().format(formatter))
    }

    Scaffold (floatingActionButton = {
        FloatingActionButton(
        modifier = Modifier,
        onClick = { showCreateFasciaOrariaDialog.value = true
        },
    )
    { Icon(Icons.Default.Add, contentDescription = "Aggiungi fascia oraria") }
    }
    ){
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp, alignment = Alignment.CenterHorizontally)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Seleziona giorno")
                DatePickerDocked(selectedDate)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Fasce Orarie:")
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    items(fasceOrarie.size) { index ->
                        val fasciaOraria = fasceOrarie[index]
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FasciaOrariaCard(fasciaOraria)
                            Button(onClick = {
                                viewModel.setSelectedFasciaOraria(orarioInizio, orarioFine, fasciaOraria)
                                showEditFasciaOrariaDialog.value = true}
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Modifica fascia oraria"
                                )
                            }
                            Button(onClick = {viewModel.deleteFasciaOraria(fasciaOraria)}
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


        if(showCreateFasciaOrariaDialog.value) {
            FasciaOrariaDialog(Mode.CREATE, onDismissRequest = {showCreateFasciaOrariaDialog.value = false},
                orarioInizio, orarioFine) {
                createButton("Crea fascia oraria") {

                    viewModel.viewModelScope.launch {
                        viewModel.creaFasciaOraria(orarioInizio, orarioFine, selectedDate)
                    }
                }

            }
        }
        if (showEditFasciaOrariaDialog.value) {
            FasciaOrariaDialog(Mode.EDIT,
                onDismissRequest = {
                    viewModel.setDefaultFasciaOrariaState(orarioInizio, orarioFine)
                    showEditFasciaOrariaDialog.value = false
                },
                orarioInizio, orarioFine
            ) {
                createButton("Modifica fascia oraria") {

                    viewModel.viewModelScope.launch {
                        viewModel.modificaFasciaOraria()
                    }
                }

            }
        }

    }




}



@Composable
fun DatePickerDocked(selectedDate: MutableState<String>) {
    var showDatePicker: Boolean by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()


    Box(
        modifier = Modifier.wrapContentWidth()
    ) {
        OutlinedTextField(

            value = selectedDate.value,
            onValueChange = { text ->
                selectedDate.value = text
//                runCatching {
//                    val date = LocalDate.parse(text, formatter)
//                    datePickerState.selectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
//                }
            },
//            label = { Text("dd/mm/yyyy") },
//            placeholder = { Text("dd/mm/yyyy") },
//            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .requiredWidth(159.dp)
                .height(64.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.hasFocus)
                        setDate(selectedDate, formatter, datePickerState)
                }
                .onKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        setDate(selectedDate, formatter, datePickerState)
                        return@onKeyEvent true
                    } else return@onKeyEvent false
                }

        )


        if (showDatePicker) {
//            Popup(
//                onDismissRequest = { showDatePicker = false },
//                alignment = Alignment.TopStart
//            ) {
            Box(
                modifier = Modifier
                    .requiredWidth(250.dp)
                    .offset(y = 64.dp)
                    .shadow(elevation = 4.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
//            }
        }
    }
}

private fun BoxScope.setDate(
    selectedDate: MutableState<String>,
    formatter: DateTimeFormatter,
    datePickerState: DatePickerState
) {
    runCatching {
        val date = LocalDate.parse(selectedDate.value, formatter)
        datePickerState.setSelectedDate(date)
    }
}


fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}


