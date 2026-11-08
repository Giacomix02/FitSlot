package it.univaq.grr.fitslot.domain.models

data class User (
    val id: Long = 0,
    val email: String,
    val password: String,
    val username: String,
    val nome: String,
    val cognome: String,
    val dataNascita: String,
    val telefono: String,
    val indirizzo: String,
    val citta: String,
    val emailContatto: String,
    val ruolo: Ruolo
)