package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.UserDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
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

    override suspend fun userRegistration(user: User): Long {
        val salt = PasswordSecurity.generateSalt()

        val userToSave = User(
            email = user.email,
            username = user.username,
            password = PasswordSecurity.hashPassword(user.password, salt),
            nome = user.nome,
            cognome = user.cognome,
            dataNascita = user.dataNascita,
            telefono = user.telefono,
            indirizzo = user.indirizzo,
            citta = user.citta,
            emailContatto = user.emailContatto,
            ruolo = user.ruolo
        )

        val registrationResult = userDao.insertUser(userToSave.toEntity(salt))
        return registrationResult
    }

}