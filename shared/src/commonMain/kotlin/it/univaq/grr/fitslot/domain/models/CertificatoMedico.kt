package it.univaq.grr.fitslot.domain.models

class CertificatoMedico (
    val id: Long = 0,
    val path: String,
    val scadenza: String,
    val utente: User
)