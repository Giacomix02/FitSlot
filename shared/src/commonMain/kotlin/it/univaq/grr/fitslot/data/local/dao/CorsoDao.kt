package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import it.univaq.grr.fitslot.data.local.entity.CorsoEntity

@Dao
interface CorsoDao {
    @Insert
    suspend fun insertAll(vararg corsi: CorsoEntity)

    @Delete
    suspend fun delete(corso: CorsoEntity)

    @Query("Select * from corso")
    suspend fun getALl(): List<CorsoEntity>
}