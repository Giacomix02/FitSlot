package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.Corso

interface CorsoRepository {
    suspend fun creaCorso(corso: Corso)
}