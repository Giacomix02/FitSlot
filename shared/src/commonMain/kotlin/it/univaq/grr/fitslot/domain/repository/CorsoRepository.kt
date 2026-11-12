package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria

interface CorsoRepository {
    suspend fun creaCorso(corso: Corso)

    /** Persiste le fasce orarie selezionabili, necessarie per i controlli di sovrapposizione. */
    suspend fun salvaFasceOrarie(fasceOrarie: List<FasciaOraria>)

    /** Vero se la sala è già occupata da un corso in una fascia che si sovrappone a [fasciaOraria]. */
    suspend fun salaOccupata(salaId: Long, fasciaOraria: FasciaOraria): Boolean

    /** Id delle sale occupate in una fascia che si sovrappone a [fasciaOraria]. */
    suspend fun saleOccupate(fasciaOraria: FasciaOraria): Set<Long>

    /** Fasce orarie già occupate nella sala indicata. */
    suspend fun fasceOccupate(salaId: Long): List<FasciaOraria>
}
