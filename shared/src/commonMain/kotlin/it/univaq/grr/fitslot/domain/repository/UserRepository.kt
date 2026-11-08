package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.User

interface UserRepository {
    suspend fun serveConfigurazione(): Boolean
    suspend fun creaPrimoGestore(email: String, password: String, nome: String): User

    suspend fun getUserByEmail(email: String): User?
    suspend fun getUserById(id: Long): User?

    suspend fun userLogin(email: String, password: String): User?

}
