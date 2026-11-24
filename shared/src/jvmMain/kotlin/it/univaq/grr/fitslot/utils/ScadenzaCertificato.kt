package it.univaq.grr.fitslot.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

actual fun dataScadenza(valore: String?): String? {
    if (valore == null) return null
    val millis = valore.toLongOrNull()
    if (millis != null) return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
    return try { LocalDate.parse(valore).toString() } catch (e: DateTimeParseException) { null }
}
actual fun dataOggi(): String = LocalDate.now().toString()
actual fun aggiungiGiorni(data: String, giorni: Int): String = LocalDate.parse(data).plusDays(giorni.toLong()).toString()
actual fun dataInMillisecondi(data: String): Long = LocalDate.parse(data).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
