package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.*
import it.univaq.grr.fitslot.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/** Le scritture con controlli vengono eseguite nella stessa transazione. */
@Dao
abstract class CatalogoDao {

    @Query("SELECT * FROM abbonamenti ORDER BY nomeNormalizzato")
    abstract fun osservaAbbonamenti(): Flow<List<AbbonamentoEntity>>

    @Query("SELECT * FROM abbonamenti WHERE id = :id")
    abstract suspend fun leggiAbbonamento(id: Long): AbbonamentoEntity?

    @Query("SELECT COUNT(*) FROM abbonamenti WHERE nomeNormalizzato = :nome AND id != :id")
    abstract suspend fun duplicatiAbbonamento(nome: String, id: Long): Int

    @Insert
    abstract suspend fun inserisciAbbonamento(entity: AbbonamentoEntity): Long

    @Update
    abstract suspend fun aggiornaAbbonamento(entity: AbbonamentoEntity): Int

    @Query("SELECT COUNT(*) FROM iscrizioni_abbonamenti WHERE abbonamentoId = :id")
    abstract suspend fun iscrittiAbbonamento(id: Long): Int

    @Query("DELETE FROM abbonamenti WHERE id = :id AND NOT EXISTS (SELECT 1 FROM iscrizioni_abbonamenti WHERE abbonamentoId = :id)")
    abstract suspend fun cancellaAbbonamentoSeLibero(id: Long): Int

    @Transaction
    open suspend fun salvaAbbonamento(entity: AbbonamentoEntity): Long {
        require(duplicatiAbbonamento(entity.nomeNormalizzato, entity.id) == 0) {
            "Il nome è già in uso da un altro abbonamento."
        }
        if (entity.id == 0L) return inserisciAbbonamento(entity)
        check(aggiornaAbbonamento(entity) == 1) { "Il abbonamento non esiste più. Ricarica l'elenco." }
        return entity.id
    }

    @Transaction
    open suspend fun eliminaAbbonamento(id: Long) {
        check(iscrittiAbbonamento(id) == 0) {
            "Impossibile eliminare: sono presenti iscritti al abbonamento."
        }
        check(cancellaAbbonamentoSeLibero(id) == 1) { "Il abbonamento non esiste più. Ricarica l'elenco." }
    }

    @Query("SELECT * FROM piani_a_punti ORDER BY nomeNormalizzato")
    abstract fun osservaPianiAPunti(): Flow<List<PianoAPuntiEntity>>

    @Query("SELECT * FROM piani_a_punti WHERE id = :id")
    abstract suspend fun leggiPianoAPunti(id: Long): PianoAPuntiEntity?

    @Query("SELECT COUNT(*) FROM piani_a_punti WHERE nomeNormalizzato = :nome AND id != :id")
    abstract suspend fun duplicatiPianoAPunti(nome: String, id: Long): Int

    @Insert
    abstract suspend fun inserisciPianoAPunti(entity: PianoAPuntiEntity): Long

    @Update
    abstract suspend fun aggiornaPianoAPunti(entity: PianoAPuntiEntity): Int

    @Query("SELECT COUNT(*) FROM iscrizioni_piani_a_punti WHERE pianoId = :id")
    abstract suspend fun iscrittiPianoAPunti(id: Long): Int

    @Query("DELETE FROM piani_a_punti WHERE id = :id AND NOT EXISTS (SELECT 1 FROM iscrizioni_piani_a_punti WHERE pianoId = :id)")
    abstract suspend fun cancellaPianoAPuntiSeLibero(id: Long): Int

    @Transaction
    open suspend fun salvaPianoAPunti(entity: PianoAPuntiEntity): Long {
        require(duplicatiPianoAPunti(entity.nomeNormalizzato, entity.id) == 0) {
            "Il nome è già in uso da un altro piano a punti."
        }
        if (entity.id == 0L) return inserisciPianoAPunti(entity)
        check(aggiornaPianoAPunti(entity) == 1) { "Il piano a punti non esiste più. Ricarica l'elenco." }
        return entity.id
    }

    @Transaction
    open suspend fun eliminaPianoAPunti(id: Long) {
        check(iscrittiPianoAPunti(id) == 0) {
            "Impossibile eliminare: sono presenti iscritti al piano a punti."
        }
        check(cancellaPianoAPuntiSeLibero(id) == 1) { "Il piano a punti non esiste più. Ricarica l'elenco." }
    }

    // Queste associazioni sono il punto di integrazione con la gestione iscritti.
    @Insert
    abstract suspend fun associaAbbonamento(iscrizione: IscrizioneAbbonamentoEntity)

    @Insert
    abstract suspend fun associaPianoAPunti(iscrizione: IscrizionePianoAPuntiEntity)
}
