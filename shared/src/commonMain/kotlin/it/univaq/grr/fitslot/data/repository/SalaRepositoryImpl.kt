package it.univaq.grr.fitslot.data.repository
import it.univaq.grr.fitslot.data.local.dao.SalaDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.PermessiGestione
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import kotlinx.coroutines.flow.map
class SalaRepositoryImpl(
    private val salaDao: SalaDao,
    private val permessi: PermessiGestione = PermessiGestioneDaLocalStorage
) : SalaRepository {
    override fun osservaSale() = salaDao.osservaSale().map { sale -> sale.map { it.toDomain() } }
    override suspend fun inserisci(sala: Sala): Long {
        permessi.richiediGestore()
        valida(sala)
        return salaDao.inserisci(sala.copy(nome = sala.nome.trim()).toEntity())
    }
    override suspend fun modifica(sala: Sala) {
        permessi.richiediGestore()
        valida(sala)
        // Mantiene l'ID, quindi i corsi rimangono collegati alla stessa sala.
        check(salaDao.aggiorna(sala.copy(nome = sala.nome.trim()).toEntity()) == 1) {
            "La sala non esiste più. Ricarica l'elenco."
        }
    }
    override suspend fun elimina(id: Long) {
        permessi.richiediGestore()
        salaDao.eliminaSeLibera(id)
    }
    private fun valida(sala: Sala) {
        require(sala.nome.isNotBlank()) { "Inserisci il nome della sala." }
        require(sala.capienza > 0) { "La capienza deve essere maggiore di zero." }
    }
}
