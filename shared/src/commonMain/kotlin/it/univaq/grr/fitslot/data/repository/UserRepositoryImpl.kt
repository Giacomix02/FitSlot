package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.UserDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.UserRepository
import it.univaq.grr.fitslot.utils.PasswordSecurity



class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun serveConfigurazione(): Boolean = userDao.contaUtenti() == 0

    override suspend fun creaPrimoGestore(email: String, password: String, nome: String): User {
        require(nome.isNotBlank()) { "Inserisci il nome del gestore." }
        require(Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+").matches(email.trim())) { "Inserisci un'email valida." }
        require(password.length >= 8) { "La password deve contenere almeno 8 caratteri." }
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val salt = PasswordSecurity.generateSalt()
            val user = it.univaq.grr.fitslot.data.local.entity.UserEntity(
                email = email.trim(), password = PasswordSecurity.hashPassword(password, salt),
                salt = salt, username = email.trim(), nome = nome.trim(), cognome = "",
                dataNascita = "", telefono = "", indirizzo = "", citta = "", emailContatto = email.trim(),
                ruolo = it.univaq.grr.fitslot.domain.models.Ruolo.GESTORE)
            val id = userDao.creaPrimoGestore(user)
            user.copy(id = id).toDomain()
        }
    }


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
