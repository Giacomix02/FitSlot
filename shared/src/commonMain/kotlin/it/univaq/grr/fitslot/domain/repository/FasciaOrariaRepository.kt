package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.FasciaOraria
import kotlinx.coroutines.flow.Flow

interface FasciaOrariaRepository {

    fun observeFasceOrarie(): Flow<List<FasciaOraria>>
    suspend fun createFasciaOraria(fasciaOraria: FasciaOraria)
    suspend fun editFasciaOraria(fasciaOraria: FasciaOraria)
    suspend fun deleteFasciaOraria(fasciaOraria: FasciaOraria)
}