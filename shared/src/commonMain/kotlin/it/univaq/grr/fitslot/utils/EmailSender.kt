package it.univaq.grr.fitslot.utils

expect object EmailSender {
    suspend fun sendWelcomeEmail(toEmail: String, usernameWelcome: String, matricola: String, senderEmail: String, appPassword: String): Result<Unit>
}