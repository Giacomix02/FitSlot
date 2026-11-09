package it.univaq.grr.fitslot.data.local.converter

import androidx.room3.ColumnTypeConverter
import it.univaq.grr.fitslot.domain.models.Ruolo


class Converters {
   @ColumnTypeConverter
   fun fromRuolo(ruolo: Ruolo): String {
       return ruolo.name
   }

    @ColumnTypeConverter
    fun toRuolo(value: String): Ruolo {
       return Ruolo.valueOf(value)
   }
}