package it.univaq.grr.fitslot.domain

import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Conserva solo i dati necessari alla sessione, senza password o hash. */
data class UtenteAutenticato(val id: Long, val nome: String, val ruolo: Ruolo)

class SessioneUtente {
    private val _utente = MutableStateFlow<UtenteAutenticato?>(null)
    val utente = _utente.asStateFlow()

    fun accedi(user: User) {
        _utente.value = UtenteAutenticato(user.id, user.nome, user.ruolo)
    }

    fun esci() { _utente.value = null }

    // Il controllo viene ripetuto anche nei repository che modificano i dati.
    fun richiediGestore() {
        check(_utente.value?.ruolo == Ruolo.GESTORE) {
            "Operazione consentita solo al gestore autenticato."
        }
    }
}
