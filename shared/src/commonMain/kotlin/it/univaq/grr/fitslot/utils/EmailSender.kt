package it.univaq.grr.fitslot.utils

interface EmailSender {
    suspend fun sendWelcomeEmail(toEmail: String, username: String): Result<Unit>
}