package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownSelector(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() },
    placeholder: String? = null,
    isRequired: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    dropdownOffset: Dp = 0.dp,
    customItemContent: (@Composable (T) -> Unit)? = null,
    searchable: Boolean = false,
    searchFilter: ((item: T, query: String) -> Boolean)? = null,
    onSearchQueryChanged: (String) -> Unit = {},
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(selectedItem?.let(itemLabel) ?: "") }
    val isError = errorMessage != null

    LaunchedEffect(selectedItem) {
        searchText = selectedItem?.let(itemLabel) ?: ""
    }

    val filteredItems = remember(items, searchText, searchFilter) {
        if (!searchable || searchText.isBlank()) {
            items
        } else {
            items.filter { item ->
                searchFilter?.invoke(item, searchText)
                    ?: itemLabel(item).contains(searchText, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (searchable) searchText else selectedItem?.let(itemLabel) ?: "",
            onValueChange = { query ->
                if (searchable) {
                    searchText = query
                    expanded = true
                    onSearchQueryChanged(query)
                }
            },
            readOnly = !searchable,
            enabled = enabled,
            label = {
                Text(if (isRequired) "$label *" else label)
            },
            placeholder = placeholder?.let { { Text(it) } },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            isError = isError,
            supportingText = if (isError) {
                {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    if (searchable)
                        ExposedDropdownMenuAnchorType.PrimaryEditable
                    else
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled
                )
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.offset(y = dropdownOffset)
        ) {
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = {
                        if (customItemContent != null) {
                            customItemContent(item)
                        } else {
                            Text(
                                text = itemLabel(item),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        if (searchable) {
                            searchText = itemLabel(item)
                        }
                        onItemSelected(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}