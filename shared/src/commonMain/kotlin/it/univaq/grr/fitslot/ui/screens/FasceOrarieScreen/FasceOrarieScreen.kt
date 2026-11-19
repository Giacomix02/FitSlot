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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import it.univaq.grr.fitslot.ui.components.FasciaOrariaCard
import it.univaq.grr.fitslot.ui.components.createButton
import it.univaq.grr.fitslot.utils.parseCatching
import it.univaq.grr.fitslot.utils.toggle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


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

    val fasceOrarie by viewModel.fasceOrarie.collectAsStateWithLifecycle()

    Scaffold (floatingActionButton = {
        FloatingActionButton(
        modifier = Modifier,
        onClick = { showCreateFasciaOrariaDialog.toggle()
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
                    fasceOrarie?.filter { fasciaOraria ->
                        fasciaOraria.orarioInizio.toLocalDate().equals(
                                parseCatching(selectedDate.value, formatter)
                            )

                        }
                    ?.let {list ->
                    if (list.isNotEmpty())
                    items(list.size) { index ->


                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FasciaOrariaCard(list[index])
                            Button(onClick = {
                                viewModel.setSelectedFasciaOraria(orarioInizio, orarioFine, list[index])
                                showEditFasciaOrariaDialog.toggle()}
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


        if(showCreateFasciaOrariaDialog.value) {
            FasciaOrariaDialog(Mode.CREATE, onDismissRequest = showCreateFasciaOrariaDialog::toggle,
                orarioInizio, orarioFine) {
                createButton("Crea fascia oraria") {

                    viewModel.viewModelScope.launch {
                        viewModel.creaFasciaOraria(orarioInizio, orarioFine, selectedDate)
                        showCreateFasciaOrariaDialog.toggle()
                    }
                }

            }
        }
        if (showEditFasciaOrariaDialog.value) {
            FasciaOrariaDialog(Mode.EDIT,
                onDismissRequest = {
                    viewModel.setDefaultFasciaOrariaState(orarioInizio, orarioFine)
                    showEditFasciaOrariaDialog.toggle()
                },
                orarioInizio, orarioFine
            ) {
                createButton("Modifica fascia oraria") {

                    viewModel.viewModelScope.launch {
                        viewModel.modificaFasciaOraria(orarioInizio, orarioFine, selectedDate)
                        showEditFasciaOrariaDialog.toggle()
                    }
                }

            }
        }

    }
}



@Composable
fun DatePickerDocked(selectedDate: MutableState<String>) {
    var showDatePicker: Boolean by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDate = parseCatching(selectedDate.value, formatter))

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            selectedDate.value = convertMillisToDate(millis)
        }
    }

    Column(horizontalAlignment =  Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentWidth()
    ) {
        OutlinedTextField(

            value = selectedDate.value,
            onValueChange = { text ->
                selectedDate.value = text
                setDate(selectedDate, formatter, datePickerState)
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
                },
//                .onKeyEvent { event ->
//                    if (event.key == Key.Enter) {
//                        setDate(selectedDate, formatter, datePickerState)
//                        showDatePicker = false
//                        return@onKeyEvent true
//                    } else return@onKeyEvent false
//                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { setDate(selectedDate, formatter, datePickerState)
                    showDatePicker = false
                },
                onGo = { /* do something */},
                onNext = { /* do something */},
                onPrevious = { /* do something */},
                onSearch = { /* do something */},
                onSend = { /* do something */})

        )


        if (showDatePicker) {
//            Popup(
//                onDismissRequest = { showDatePicker = false },
//                alignment = Alignment.TopStart
//            ) {
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
//            }
        }
    }
}

private fun ColumnScope.setDate(
    selectedDate: MutableState<String>,
    formatter: DateTimeFormatter,
    datePickerState: DatePickerState
) {

        val date = parseCatching(selectedDate.value, formatter)
        datePickerState.setSelectedDate(date)

}


fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}


