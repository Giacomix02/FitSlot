package it.univaq.grr.fitslot.domain.models

data class Corso(
    val id: Long = 0,
    var name: String,
    var salaAssegnata: Sala,
    var fasciaOraria: FasciaOraria
) {
    init {
        require (name.isNotBlank()) { "Nome non può essere vuoto" }
        require (! salaAssegnata.isAlreadyOccupiedAtFasciaOraria(fasciaOraria)
        ) { "Sala assegnata è già occupata" }
    }
}