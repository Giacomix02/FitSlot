package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.SalaDao
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.SalaRepository

/** Anche il salvataggio delle sale richiede l'accesso come gestore. */
class SalaRepositoryImpl(
    private val salaDao: SalaDao,
    private val sessione: SessioneUtente
) : SalaRepository {
    override suspend fun inserisci(sala: Sala): Long {
        sessione.richiediGestore()
        require(sala.nome.isNotBlank()) { "Inserisci il nome della sala." }
        require(sala.capienza > 0) { "La capienza deve essere maggiore di zero." }
        return salaDao.inserisci(sala.toEntity())
    }
}
