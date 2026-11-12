package it.univaq.grr.fitslot.domain.models

data class Corso(
    val id: Long = 0,
    var name: String,
    var salaAssegnata: Sala,
    var fasciaOraria: FasciaOraria
) {
    init {
        require (name.isNotBlank()) { "Nome non può essere vuoto" }
        // La disponibilità della sala nella fascia oraria dipende dai corsi
        // salvati nel database: viene verificata da CorsoRepositoryImpl.
    }
}