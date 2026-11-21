package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy.Companion.ABORT
import it.univaq.grr.fitslot.data.local.entity.LogAccessoEntity


@Dao
interface LogAccessoDao {

    @Insert(onConflict = ABORT)
    suspend fun insert(logAccesso: LogAccessoEntity): Long

}