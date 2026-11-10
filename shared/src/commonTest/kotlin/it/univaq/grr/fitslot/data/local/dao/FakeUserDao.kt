package it.univaq.grr.fitslot.data.local.dao

import it.univaq.grr.fitslot.data.local.entity.UserEntity

class FakeUserDao(
    private val users: MutableList<UserEntity> = mutableListOf()
) : UserDao {

    override suspend fun getUserByEmail(email: String): UserEntity? =
        users.firstOrNull { it.email == email }

    override suspend fun getUserById(id: Long): UserEntity? =
        users.firstOrNull { it.id == id }

    fun addUser(user: UserEntity) {
        users.add(user)
    }

    fun clear() {
        users.clear()
    }
}
