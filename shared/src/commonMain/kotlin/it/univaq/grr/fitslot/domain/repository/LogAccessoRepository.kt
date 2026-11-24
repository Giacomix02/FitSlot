package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.LogAccesso
import it.univaq.grr.fitslot.domain.models.User

interface LogAccessoRepository {

    suspend fun insertAccesso(accesso: LogAccesso): Long

    suspend fun getAccessiByUser(user: User): List<LogAccesso>

    suspend fun getAll(): List<LogAccesso>
}