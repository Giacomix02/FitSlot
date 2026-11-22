package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.CertificatiReceptionDao
import it.univaq.grr.fitslot.domain.PermessiReception
import it.univaq.grr.fitslot.domain.models.IscrittoCertificato
import it.univaq.grr.fitslot.utils.dataScadenza
import java.time.LocalDate
import it.univaq.grr.fitslot.data.local.dao.UserDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.domain.models.User

class CertificatiReceptionRepository(private val dao: CertificatiReceptionDao, private val utenti: UserDao) {
    suspend fun elenco(scaduti: Boolean, dal: LocalDate, al: LocalDate,
                       oggi: LocalDate = LocalDate.now()): List<IscrittoCertificato> {
        PermessiReception.richiediReception()
        val risultato = mutableListOf<IscrittoCertificato>()
        for (iscritto in dao.iscritti()) {
            val scadenza = dataScadenza(iscritto.scadenza) ?: continue
            if (scaduti && scadenza < oggi) risultato.add(iscritto)
            else if (!scaduti && scadenza >= oggi && scadenza >= dal && scadenza <= al) risultato.add(iscritto)
        }
        return risultato
    }

    suspend fun iscritti(): List<IscrittoCertificato> {
        PermessiReception.richiediReception()
        return dao.iscritti()
    }
    suspend fun utente(id: Long): User {
        PermessiReception.richiediReception()
        return checkNotNull(utenti.getUserById(id)) { "Iscritto non trovato." }.toDomain()
    }

}
