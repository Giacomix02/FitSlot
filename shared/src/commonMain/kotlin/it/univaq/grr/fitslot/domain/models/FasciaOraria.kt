package it.univaq.grr.fitslot.domain.models

import java.time.LocalDateTime

data class FasciaOraria(
    val id: Long,
    var orarioInizio: LocalDateTime,
    var orarioFine: LocalDateTime
) {
    var durata: Int = orarioFine.hour.minus(orarioInizio.hour)

    init {
        require(durata>0) {"Orari devono essere cronologicamente sensati"}
        require(durata < DURATA_MASSIMA_ORE) {"Durata deve essere minore di $DURATA_MASSIMA_ORE"}
    }
    companion object {
        // Define the global constraint here
        // Example: Max duration is 4 hours
        const val DURATA_MASSIMA_ORE = 4
    }
}
