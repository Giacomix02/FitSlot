package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "fascia_oraria")
data class FasciaOrariaEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var orarioInizio: LocalDateTime, var orarioFine: LocalDateTime) {
    var durata: Int = orarioFine.hour.minus(orarioInizio.hour)
}
