package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import it.univaq.grr.fitslot.data.local.entity.UserEntity

@Dao
interface UserDao {

    //UserEntity è nullable, quindi se non trova l'utente ritorna null, altrimenti ritorna l'utente

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long
}