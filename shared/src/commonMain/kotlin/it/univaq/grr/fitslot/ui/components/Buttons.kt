package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
public fun createButton(
    text: String = "crea",
    createFunction: () -> Unit = {},
) {
    Button(onClick = createFunction) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text)
            Icon(Icons.Default.Add, contentDescription = text)
        }
    }
}