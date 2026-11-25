package it.univaq.grr.fitslot.utils

import androidx.compose.ui.graphics.ImageBitmap
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

expect fun parseCatching (text: CharSequence, formatter: DateTimeFormatter,
                       doThisWhenCatching: (e: DateTimeParseException) -> Unit = {} ): LocalDate?

expect fun ByteArray.toImageBitmap(): ImageBitmap