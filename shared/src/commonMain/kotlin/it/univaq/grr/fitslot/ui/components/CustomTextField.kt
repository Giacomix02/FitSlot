package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    placeholder: String? = null,
) {
    val isError = errorMessage != null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(if (isRequired) "$label *" else label)
        },
        placeholder = { Text(placeholder ?: "") },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
//        keyboardActions = KeyboardActions(
//            onDone = {
//                setToDatePicker(text, formatter, datePickerState, setShowDateNotValidError)
//                showDatePicker = false
//            },
//            onGo = { /* do something */},
//            onNext = { /* do something */},
//            onPrevious = { /* do something */},
//            onSearch = { /* do something */},
//            onSend = { /* do something */}),

        modifier = modifier
    )
}