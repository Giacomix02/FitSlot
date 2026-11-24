package it.univaq.grr.fitslot.domain.models

data class LogAccesso (
    val id : Long = 0,
    val user: User,
    val timestamp: Long
)