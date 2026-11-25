package it.univaq.grr.fitslot.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.*

actual fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault()
    return formatter.format(Date(millis))
}

actual fun Long.isMillisBeforeToday(): Boolean {
    return Instant.ofEpochMilli(this)
        .atZone(TimeZone.getDefault().toZoneId())
        .toLocalDate().isAfter(LocalDate.now())
}
