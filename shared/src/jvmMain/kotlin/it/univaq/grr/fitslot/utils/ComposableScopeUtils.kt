package it.univaq.grr.fitslot.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

actual fun parseCatching (text: CharSequence, formatter: DateTimeFormatter,
                   doThisWhenCatching: (e: DateTimeParseException) -> Unit  ): LocalDate? {
    var result: LocalDate? = null
    try {
        result = LocalDate.parse(text, formatter)
    } catch (e: DateTimeParseException) {
        doThisWhenCatching(e)
    }
    return result
}

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}