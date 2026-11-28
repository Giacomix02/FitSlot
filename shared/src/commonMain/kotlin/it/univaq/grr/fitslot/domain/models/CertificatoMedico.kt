package it.univaq.grr.fitslot.domain.models

class CertificatoMedico (
    val id: Long = 0,
    val path: String,
    // in millisecondi da epoch
    val scadenza: Long,
    val utente: User
)