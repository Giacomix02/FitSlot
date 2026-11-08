package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.*
import it.univaq.grr.fitslot.data.local.entity.UserEntity

@Dao
abstract class UserDao {
    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    abstract suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM user WHERE id = :id")
    abstract suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM user")
    abstract suspend fun contaUtenti(): Int

    @Insert
    abstract suspend fun inserisci(user: UserEntity): Long

    // Il controllo e l'inserimento sono atomici: non si può creare un secondo gestore da qui.
    @Transaction
    open suspend fun creaPrimoGestore(user: UserEntity): Long {
        check(contaUtenti() == 0) { "La configurazione iniziale è già stata completata. Accedi con il tuo account." }
        return inserisci(user)
    }
}
