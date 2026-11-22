package it.univaq.grr.fitslot.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

/** Il caricamento del team salva la data del selettore come millisecondi UTC. */
fun dataScadenza(valore: String?): LocalDate? {
    if (valore == null) return null
    val millisecondi = valore.toLongOrNull()
    if (millisecondi != null) return Instant.ofEpochMilli(millisecondi).atZone(ZoneOffset.UTC).toLocalDate()
    return try {
        LocalDate.parse(valore)
    } catch (e: DateTimeParseException) {
        null
    }
}
