package it.univaq.grr.fitslot.data.local.dao

import androidx.sqlite.SQLiteException
import it.univaq.grr.fitslot.data.local.entity.UserEntity

class FakeUserDao(
    private val users: MutableList<UserEntity> = mutableListOf()
) : UserDao {

    override suspend fun getUserByEmail(email: String): UserEntity? =
        users.firstOrNull { it.email == email }

    override suspend fun getUserById(id: Long): UserEntity? =
        users.firstOrNull { it.id == id }

    override suspend fun insertUser(user: UserEntity): Long {
        if (users.any { it.email == user.email }) {
            throw SQLiteException("User with email ${user.email} already exists")
        }else{
            users.add(user)
            return user.id
        }
    }

    fun addUser(user: UserEntity) {
        users.add(user)
    }

    fun clear() {
        users.clear()
    }
}
