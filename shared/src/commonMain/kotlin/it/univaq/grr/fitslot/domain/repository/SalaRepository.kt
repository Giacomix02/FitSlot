package it.univaq.grr.fitslot.domain.repository
import it.univaq.grr.fitslot.domain.models.Sala
import kotlinx.coroutines.flow.Flow
/** Operazioni della GRR-14. */
interface SalaRepository {
    fun osservaSale(): Flow<List<Sala>>
    suspend fun inserisci(sala: Sala): Long
    suspend fun modifica(sala: Sala)
    suspend fun elimina(id: Long)
}
