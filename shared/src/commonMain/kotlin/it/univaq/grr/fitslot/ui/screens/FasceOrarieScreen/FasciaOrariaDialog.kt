package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Mode {
    CREATE,
    EDIT
}

@Composable
fun FasciaOrariaDialog(mode: Mode, onDismissRequest: () -> Unit,
                       orarioInizioState: TimePickerState = rememberTimePickerState(),
                       orarioFineState: TimePickerState = rememberTimePickerState(),
                       button: @Composable (() -> Unit)  ) {
    TimePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = button,
        title = { Text("${if (mode == Mode.CREATE) "Crea" else "Modifica"} fascia oraria") },
//        modeToggleButton = {Button(onClick = {}){Text("Change")} }
    ) {
        Row() {
            TimeInput(
                state = orarioInizioState,
            )
            Icon(
                Icons.Default.KeyboardDoubleArrowRight,
                contentDescription = "Da ora a ora",
                modifier = Modifier.size(64.dp).padding(top = 10.dp)
            )
            TimeInput(state = orarioFineState)
        }
    }
}

