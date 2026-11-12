package it.univaq.grr.fitslot.data.local.dao
import androidx.room3.*
import it.univaq.grr.fitslot.data.local.entity.SalaEntity
import kotlinx.coroutines.flow.Flow
@Dao
abstract class SalaDao {
    @Insert abstract suspend fun inserisci(sala: SalaEntity): Long
    @Query("SELECT * FROM sale ORDER BY nome COLLATE NOCASE, id")
    abstract fun osservaSale(): Flow<List<SalaEntity>>
    @Query("SELECT * FROM sale WHERE id = :id")
    abstract suspend fun leggi(id: Long): SalaEntity?
    @Update abstract suspend fun aggiorna(sala: SalaEntity): Int
    // Controllo ed eliminazione avvengono nella stessa istruzione SQL.
    @Query("DELETE FROM sale WHERE id = :id AND NOT EXISTS (SELECT 1 FROM corso WHERE salaAssegnataId = :id)")
    abstract suspend fun cancellaSeLibera(id: Long): Int
    @Transaction
    open suspend fun eliminaSeLibera(id: Long) {
        if (cancellaSeLibera(id) == 1) return
        check(leggi(id) != null) { "La sala non esiste più. Ricarica l'elenco." }
        error("Impossibile eliminare la sala: è assegnata a uno o più corsi.")
    }
}
