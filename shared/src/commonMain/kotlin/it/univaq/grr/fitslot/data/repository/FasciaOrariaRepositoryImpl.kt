package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.FasciaOrariaDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.repository.FasciaOrariaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FasciaOrariaRepositoryImpl(private val fasciaOrariaDao: FasciaOrariaDao) : FasciaOrariaRepository {
    override fun observeFasceOrarie(): Flow<List<FasciaOraria>> {
        return fasciaOrariaDao.observeFasceOrarie().map { it.map { fasciaOrariaEntity -> fasciaOrariaEntity.toDomain() } }
    }

    override suspend fun createFasciaOraria(fasciaOraria: FasciaOraria) {
        return fasciaOrariaDao.insertAll(fasciaOraria.toEntity())
    }

    override suspend fun editFasciaOraria(fasciaOraria: FasciaOraria) {
        return fasciaOrariaDao.updateFasciaOraria(fasciaOraria.toEntity())
    }

    override suspend fun deleteFasciaOraria(fasciaOraria: FasciaOraria) {
        return fasciaOrariaDao.deleteALl(fasciaOraria.toEntity())
    }
}