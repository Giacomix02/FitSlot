package it.univaq.grr.fitslot.ui.screens.certificatiReception

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.utils.aggiungiGiorni
import it.univaq.grr.fitslot.utils.dataOggi
import it.univaq.grr.fitslot.utils.dataScadenza
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CertificatiReceptionViewModel(private val repository: CertificatoMedicoRepository) : ViewModel() {
    var dal by mutableStateOf(dataOggi())
    var al by mutableStateOf(aggiungiGiorni(dataOggi(), 30))
    var elenco by mutableStateOf<List<User>>(emptyList())
        private set
    var messaggio by mutableStateOf("")
        private set
    var inCorso by mutableStateOf(false)
        private set
    var utenteScelto by mutableStateOf<User?>(null)
        private set
    var scadenze by mutableStateOf<Map<Long, String>>(emptyMap())
        private set
    private var filtro: Boolean? = null

    init { cerca(null) }

    // null mostra tutti gli iscritti, true gli scaduti, false quelli nell'intervallo.
    fun cerca(scaduti: Boolean?) {
        if (inCorso) return
        val inizio = dataScadenza(dal)
        val fine = dataScadenza(al)
        if (scaduti == false && (inizio == null || fine == null || inizio > fine)) {
            messaggio = "Inserisci un intervallo valido nel formato AAAA-MM-GG."
            return
        }
        filtro = scaduti
        inCorso = true
        elenco = emptyList()
        messaggio = ""
        viewModelScope.launch {
            try {
                val oggi = dataOggi()
                val certificati = repository.certificati(scaduti, inizio ?: oggi, fine ?: oggi, oggi)
                scadenze = certificati.associate { it.utente.id to it.scadenza.toString() }
                elenco = if (scaduti == null) repository.iscritti() else certificati.map { it.utente }
                if (elenco.isEmpty()) messaggio = "Nessun iscritto trovato."
            } catch (e: CancellationException) {
                // La cancellazione interrompe la coroutine: non è un errore da mostrare.
                throw e
            } catch (e: Exception) { messaggio = "Impossibile caricare gli iscritti. Riprova."
            } finally { inCorso = false }
        }
    }

    fun scegliIscritto(utente: User) {
        if (!inCorso) utenteScelto = utente
    }

    fun chiudiCaricamento() { utenteScelto = null }
    fun certificatoSalvato() {
        utenteScelto = null
        cerca(filtro)
    }
}
