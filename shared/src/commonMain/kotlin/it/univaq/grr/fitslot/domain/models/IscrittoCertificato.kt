package it.univaq.grr.fitslot.domain.models

/** Dati mostrati alla reception, senza password o altri dati del profilo. */
data class IscrittoCertificato(
    val id: Long,
    val nome: String,
    val cognome: String,
    val email: String,
    val scadenza: String?
)
