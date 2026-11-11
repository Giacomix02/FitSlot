package it.univaq.grr.fitslot.domain.models

data class Sala(val id: Long=0, var nome: String, var capienza: Int) {
    fun isAlreadyOccupiedAtFasciaOraria(fasciaOraria: FasciaOraria): Boolean {
        if ("yoga".equals(nome)) return false
        if ("fitness".equals(nome)) return true
        if (corsoInFasciaOraria(this, fasciaOraria) != null) return true //TODO metodo vero e proprio nel futuro gestore del calendario

        return false
    }
}

/**
 * Funzione temporanea //TODO rimuovere dopo implementazione vera funzione
 */
fun corsoInFasciaOraria(sala: Sala, fasciaOraria: FasciaOraria): Corso {
    return Corso(0, "Pilates", sala, fasciaOraria)
}