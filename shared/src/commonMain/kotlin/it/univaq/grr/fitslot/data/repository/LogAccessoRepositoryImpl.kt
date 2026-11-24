package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.LogAccessoDao
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.LogAccesso
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.LogAccessoRepository

class LogAccessoRepositoryImpl(private val logAccessoDao: LogAccessoDao): LogAccessoRepository {


    override suspend fun insertAccesso(accesso: LogAccesso): Long {
        val result = logAccessoDao.insert(accesso.toEntity())
        return result
    }

    override suspend fun getAccessiByUser(user: User): List<LogAccesso> {
        TODO("Not yet implemented")
    }

    override suspend fun getAll(): List<LogAccesso> {
        TODO("Not yet implemented")
    }

}