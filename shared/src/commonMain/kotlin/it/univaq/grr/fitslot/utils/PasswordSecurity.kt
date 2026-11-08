package it.univaq.grr.fitslot.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordSecurity {
    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH = 512
    private const val ALGORITHM = "PBKDF2WithHmacSHA512"

    // Genera un salt casuale crittograficamente sicuro (16 byte)
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    // Calcola l'hash della password con il salt dato
    fun hashPassword(password: String, saltBase64: String): String {
        val salt = Base64.getDecoder().decode(saltBase64)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    // Verifica se la password inserita corrisponde all'hash salvato
    fun verifyPassword(password: String, saltBase64: String, expectedHashBase64: String): Boolean {
        val computedHash = hashPassword(password, saltBase64)
        // Usa un confronto a tempo costante per prevenire timing attacks
        return MessageDigest.isEqual(
            Base64.getDecoder().decode(computedHash),
            Base64.getDecoder().decode(expectedHashBase64)
        )
    }
}