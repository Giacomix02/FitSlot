package it.univaq.grr.fitslot.domain.models

data class Sala(val id: Long=0, var nome: String, var capienza: Int)
// L'occupazione della sala in una fascia oraria è verificata tramite
// CorsoRepository / CorsoDao, perché dipende dai corsi salvati nel database.