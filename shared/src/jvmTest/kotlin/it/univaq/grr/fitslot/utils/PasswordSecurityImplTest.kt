package it.univaq.grr.fitslot.utils

import kotlin.test.Test

class PasswordSecurityImplTest {

    @Test
    fun `generateSalt genera un salt valido`() {
        val salt = PasswordSecurity.generateSalt()
        kotlin.test.assertTrue(salt.isNotEmpty())
    }

    @Test
    fun `hashPassword genera un hash valido`() {
        val salt = PasswordSecurity.generateSalt()
        val password = "Password123!"
        val hash = PasswordSecurity.hashPassword(password, salt)
        kotlin.test.assertTrue(hash.isNotEmpty())
    }

    @Test
    fun `verifyPassword restituisce true per credenziali valide`() {
        val salt = PasswordSecurity.generateSalt()
        val password = "Password123!"
        val hash = PasswordSecurity.hashPassword(password, salt)
        kotlin.test.assertTrue(
            PasswordSecurity.verifyPassword(password, salt, hash)
        )
    }

}