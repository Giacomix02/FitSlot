package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.UserDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.UserRepository
import it.univaq.grr.fitslot.utils.PasswordSecurity



class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun getUserByEmail(email: String): User? {
        val userEntity = userDao.getUserByEmail(email)
        return userEntity?.toDomain()
    }

    override suspend fun getUserById(id: Long): User? {
        val userEntity = userDao.getUserById(id)
        return userEntity?.toDomain()
    }

    override suspend fun userLogin(email: String, password: String): User? {
        val userEntity = userDao.getUserByEmail(email) ?: return null

        val isPasswordValid = PasswordSecurity.verifyPassword(
            password = password,
            saltBase64 = userEntity.salt,
            expectedHashBase64 = userEntity.password
        )

        return if (isPasswordValid) userEntity.toDomain() else null
    }

}