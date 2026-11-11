package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T> DropDownMenuForSelection(
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