package it.univaq.grr.fitslot.utils

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

actual object EmailSender{

    actual suspend fun sendWelcomeEmail(toEmail: String, usernameWelcome: String, senderEmail: String, appPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.starttls.required", "true")
                    // Timeout per evitare che rimanga appeso in caso di problemi di rete
                    put("mail.smtp.connectiontimeout", "5000")
                    put("mail.smtp.timeout", "5000")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, appPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail, "FitSlot"))
                    setRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                    subject = "Conferma registrazione: Benvenuto $usernameWelcome!"
                    setText(
                        """
                        Ciao $usernameWelcome,
                        
                        Il tuo account è stato creato con successo all'interno del sistema.
                        
                        Cordiali saluti,
                        Il Team di FitSlot
                        """.trimIndent(),
                        "UTF-8"
                    )
                }

                Transport.send(message)
            }
        }
}