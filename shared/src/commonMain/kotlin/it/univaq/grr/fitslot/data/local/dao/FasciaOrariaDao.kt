package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FasciaOrariaDao {

    @Query("SELECT * FROM fascia_oraria ORDER BY orarioInizio")
    fun observeFasceOrarie(): Flow<List<FasciaOrariaEntity>>

    @Insert
    suspend fun insertAll(fasciaOraria: FasciaOrariaEntity): Long

    @Update
    suspend fun updateFasciaOraria(fasciaOraria: FasciaOrariaEntity)

    @Delete
    suspend fun deleteALl(vararg fasciaOraria: FasciaOrariaEntity)
}