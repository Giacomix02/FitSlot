package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun SelezioneMultipla(label: String, elementi: List<Pair<Long, String>>, selezionati: Set<Long>,
                     inclusi: Set<Long>, abilitato: Boolean, onSeleziona: (Long) -> Unit) {
    var aperto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { aperto = true }, enabled = abilitato) {
            Text("$label (${selezionati.size} selezionati)")
        }
        DropdownMenu(expanded = aperto && abilitato, onDismissRequest = { aperto = false }) {
            for ((id, nome) in elementi) {
                DropdownMenuItem(
                    text = { Text(if (id in inclusi) "$nome (già incluso)" else nome) },
                    leadingIcon = { Checkbox(checked = id in selezionati || id in inclusi, onCheckedChange = null) },
                    enabled = id !in inclusi, onClick = { onSeleziona(id) })
            }
            DropdownMenuItem(text = { Text("Fine selezione") }, onClick = { aperto = false })
        }
    }
}
