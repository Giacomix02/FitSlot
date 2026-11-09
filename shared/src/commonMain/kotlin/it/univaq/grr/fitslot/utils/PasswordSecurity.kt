package it.univaq.grr.fitslot.utils

expect object PasswordSecurity {
    fun generateSalt(): String
    fun hashPassword(password: String, saltBase64: String): String
    fun verifyPassword(
        password: String,
        saltBase64: String,
        expectedHashBase64: String
    ): Boolean
}