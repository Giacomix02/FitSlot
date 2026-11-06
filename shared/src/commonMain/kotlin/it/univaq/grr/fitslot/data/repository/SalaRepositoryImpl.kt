package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.SalaDao
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.SalaRepository

//Implementa il salvataggio delle sale tramite il DAO di Room.

class SalaRepositoryImpl(
    private val salaDao: SalaDao
) : SalaRepository {

    override suspend fun inserisci(sala: Sala): Long {
        val entity = sala.toEntity()
        return salaDao.inserisci(entity)
    }
}