package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import it.univaq.grr.fitslot.data.local.entity.CorsoEntity
import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity

@Dao
interface CorsoDao {
    @Insert
    suspend fun insertAll(vararg corsi: CorsoEntity)

    /** Salva (o aggiorna) una fascia oraria e restituisce il suo id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFasciaOraria(fasciaOraria: FasciaOrariaEntity): Long

    /** Salva (o aggiorna) le fasce orarie usate dai corsi, così che la query di
     * sovrapposizione possa confrontare gli orari direttamente nel database. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFasceOrarie(vararg fasceOrarie: FasciaOrariaEntity)

    /** Id della fascia con gli stessi orari, se esiste già. */
    @Query("SELECT id FROM fascia_oraria WHERE orarioInizio = :orarioInizio AND orarioFine = :orarioFine LIMIT 1")
    suspend fun fasciaOrariaId(orarioInizio: String, orarioFine: String): Long?

    @Delete
    suspend fun delete(corso: CorsoEntity)

    @Query("Select * from corso")
    suspend fun getALl(): List<CorsoEntity>

    /**
     * Conta i corsi che occupano la sala [salaId] in una fascia oraria che si
     * sovrappone all'intervallo [orarioInizio, orarioFine).
     *
     * Gli orari sono salvati come testo ISO-8601 (es. "2027-06-01T15:30"),
     * quindi il confronto lessicografico coincide con l'ordine cronologico.
     * Due fasce si sovrappongono quando una inizia prima che l'altra finisca
     * e viceversa; una fascia che inizia esattamente quando l'altra finisce
     * non è considerata in conflitto.
     */
    @Query(
        """
        SELECT COUNT(*) FROM corso
        INNER JOIN fascia_oraria ON fascia_oraria.id = corso.fasciaOrariaId
        WHERE corso.salaAssegnataId = :salaId
          AND fascia_oraria.orarioInizio < :orarioFine
          AND fascia_oraria.orarioFine > :orarioInizio
        """
    )
    suspend fun contaCorsiSovrapposti(
        salaId: Long,
        orarioInizio: String,
        orarioFine: String
    ): Int

    /** Restituisce gli id delle sale occupate da almeno un corso nella fascia indicata. */
    @Query(
        """
        SELECT DISTINCT corso.salaAssegnataId FROM corso
        INNER JOIN fascia_oraria ON fascia_oraria.id = corso.fasciaOrariaId
        WHERE fascia_oraria.orarioInizio < :orarioFine
          AND fascia_oraria.orarioFine > :orarioInizio
        """
    )
    suspend fun saleOccupateInFascia(orarioInizio: String, orarioFine: String): List<Long>

    /** Restituisce le fasce orarie già occupate da almeno un corso nella sala indicata. */
    @Query(
        """
        SELECT fascia_oraria.* FROM fascia_oraria
        INNER JOIN corso ON corso.fasciaOrariaId = fascia_oraria.id
        WHERE corso.salaAssegnataId = :salaId
        """
    )
    suspend fun fasceOrarieOccupatePerSala(salaId: Long): List<FasciaOrariaEntity>
}
