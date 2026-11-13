package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


val fasceOrarie = listOf(
    FasciaOraria(1, LocalDateTime.parse("2027-06-01T15:30"),LocalDateTime.parse("2027-06-01T17:30") ),
    FasciaOraria(2, LocalDateTime.parse("2027-06-01T18:30"),LocalDateTime.parse("2027-06-01T19:30") ),
    FasciaOraria(3, LocalDateTime.parse("2027-06-02T12:30"),LocalDateTime.parse("2027-06-02T15:00") )
)

@Preview
@Composable
fun FasceOrarieScreen() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy( space = 10.dp, alignment =  Alignment.CenterHorizontally) ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Seleziona giorno")
            DatePickerDocked()
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Fasce Orarie:")
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally){
                items(fasceOrarie.size) { index ->
                    val fasciaOraria = fasceOrarie[index]
                    Card(
                        Modifier.wrapContentWidth().padding(5.dp,),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row( Modifier.align(Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                orarioDiFasciaOrariaCard(fasciaOraria.orarioInizio)
                                Icon(Icons.Default.KeyboardDoubleArrowRight, contentDescription = "dalle ore ${fasciaOraria.orarioInizio.hour} e ${fasciaOraria.orarioInizio.minute} alle ore ${fasciaOraria.orarioFine.hour} e ${fasciaOraria.orarioFine.minute}")
                                orarioDiFasciaOrariaCard(fasciaOraria.orarioFine)
                            }
                    }
                }
            }
        }


    }


    
}

@Composable
private fun orarioDiFasciaOrariaCard(orario: LocalDateTime) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH : mm")

    Card(Modifier.width(100.dp).padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text = orario.format(timeFormatter),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 3.dp),
            color = MaterialTheme.colorScheme.onPrimary)
    }
}


@Composable
fun DatePickerDocked() {
    var showDatePicker:Boolean by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val selectedDate = remember { mutableStateOf(datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: LocalDate.now().format(formatter)) }

    Box(
        modifier = Modifier.width(400.dp)
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
                .fillMaxWidth()
                .height(64.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.hasFocus)
                        setDate(selectedDate, formatter, datePickerState)
                }
                .onKeyEvent{ event ->
                    if (event.key == Key.Enter) {
                        setDate(selectedDate, formatter, datePickerState)
                        return@onKeyEvent true
                    }
                    else return@onKeyEvent false
                }

        )


        if (showDatePicker) {
//            Popup(
//                onDismissRequest = { showDatePicker = false },
//                alignment = Alignment.TopStart
//            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
    formatter: DateTimeFormatter?,
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