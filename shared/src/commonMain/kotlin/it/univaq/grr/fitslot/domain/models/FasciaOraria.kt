package it.univaq.grr.fitslot.domain.models

import java.time.Duration
import java.time.LocalDateTime

data class FasciaOraria(
    val id: Long = 0,
    var orarioInizio: LocalDateTime = LocalDateTime.now(),
    var orarioFine: LocalDateTime = LocalDateTime.now().plusHours(2)
) {
    val durata: Int
        get() = Duration.between(orarioInizio, orarioFine).toHours().toInt()

    init {
        require(durata > 0) { "Orari devono essere cronologicamente sensati" }
        require(durata < DURATA_MASSIMA_ORE) { "Durata deve essere minore di $DURATA_MASSIMA_ORE" }
    }

    companion object {
        // Define the global constraint here
        // Example: Max duration is 4 hours
        const val DURATA_MASSIMA_ORE = 4
    }

}
