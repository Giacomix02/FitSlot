package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class Mode {
    CREATE,
    EDIT
}

@Preview
@Composable
fun FasciaOrariaDialog(mode: Mode = Mode.CREATE, onDismissRequest: () -> Unit = {},
                       orarioInizioState: TimePickerState = rememberTimePickerState(),
                       orarioFineState: TimePickerState = rememberTimePickerState(),
                       erroreFasciaOraria: String = "",
                       button: @Composable (() -> Unit) = { DefaultButton() } ) {
    TimePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = button,
        title = { Text("${returnVariationByMode(mode)}fascia oraria") },
//        modeToggleButton = {Button(onClick = {}){Text("Change")} }
    ) {
        Column { Row() {
                TimeInput(
                    state = orarioInizioState,
                )
                Icon(
                    Icons.Default.KeyboardDoubleArrowRight,
                    contentDescription = "Da ora a ora",
                    modifier = Modifier.size(64.dp).padding(top = 10.dp)
                )
                TimeInput(state = orarioFineState) }
                if (erroreFasciaOraria.isNotBlank()) Text(text=erroreFasciaOraria, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun returnVariationByMode(mode: Mode):String {
    if (mode == Mode.CREATE) return "Crea "
    if (mode == Mode.EDIT) return "Modifica "

    return ""
}

@Composable
private fun DefaultButton(){
    Button(onClick = {}) {

    }
}