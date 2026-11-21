package it.univaq.grr.fitslot.utils

import androidx.compose.runtime.MutableState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


fun Boolean.toggle() {
    this.not()
}
fun MutableState<Boolean>.toggle() {
    this.value = !this.value
}

fun parseCatching (text: CharSequence, formatter: DateTimeFormatter,
                       doThisWhenCatching: (e: DateTimeParseException) -> Unit = {} ): LocalDate? {
    var result: LocalDate? = null
    try {
        result = LocalDate.parse(text, formatter)
    } catch (e: DateTimeParseException) {
        doThisWhenCatching(e)
    }
    return result
}