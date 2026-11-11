package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.CorsoDao
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.repository.CorsoRepository


sealed class CorsoRepositoryImpl (private val corsoDao: CorsoDao): CorsoRepository {
    override suspend fun creaCorso(corso: Corso) {
        corsoDao.insertAll(corso.toEntity())
    }

}