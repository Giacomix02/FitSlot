package it.univaq.grr.fitslot.data.local.converter

import androidx.room3.ColumnTypeConverter
import it.univaq.grr.fitslot.domain.models.Ruolo


class Converters {
    // Le fasce orarie vengono salvate come testo nel formato ISO.
    @ColumnTypeConverter
    fun fromDateTime(value: java.time.LocalDateTime): String = value.toString()

    @ColumnTypeConverter
    fun toDateTime(value: String): java.time.LocalDateTime = java.time.LocalDateTime.parse(value)

   @ColumnTypeConverter
   fun fromRuolo(ruolo: Ruolo): String {
       return ruolo.name
   }

    @ColumnTypeConverter
    fun toRuolo(value: String): Ruolo {
       return Ruolo.valueOf(value)
   }
}
