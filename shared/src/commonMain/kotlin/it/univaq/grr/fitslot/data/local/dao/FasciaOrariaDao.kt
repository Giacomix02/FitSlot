package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.*
import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FasciaOrariaDao {

    @Query("SELECT * FROM fascia_oraria ORDER BY orarioInizio")
    fun observeFasceOrarie(): Flow<List<FasciaOrariaEntity>>

    @Insert
    suspend fun insertAll(vararg fasceOrarie: FasciaOrariaEntity)

    @Update
    suspend fun updateFasciaOraria(fasciaOraria: FasciaOrariaEntity)

    @Delete
    suspend fun deleteALl(vararg fasciaOraria: FasciaOrariaEntity)
}