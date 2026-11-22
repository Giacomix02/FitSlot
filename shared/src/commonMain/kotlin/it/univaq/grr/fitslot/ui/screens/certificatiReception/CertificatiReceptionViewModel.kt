package it.univaq.grr.fitslot.ui.screens.certificatiReception

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.data.repository.CertificatiReceptionRepository
import it.univaq.grr.fitslot.domain.models.IscrittoCertificato
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.utils.dataScadenza
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CertificatiReceptionViewModel(private val repository: CertificatiReceptionRepository) : ViewModel() {
    var dal by mutableStateOf(LocalDate.now().toString())
    var al by mutableStateOf(LocalDate.now().plusDays(30).toString())
    var elenco by mutableStateOf<List<IscrittoCertificato>>(emptyList())
        private set
    var messaggio by mutableStateOf("")
        private set
    var inCorso by mutableStateOf(false)
        private set
    var utenteScelto by mutableStateOf<User?>(null)
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
                val oggi = LocalDate.now()
                elenco = if (scaduti == null) repository.iscritti()
                    else repository.elenco(scaduti, inizio ?: oggi, fine ?: oggi, oggi)
                if (elenco.isEmpty()) messaggio = "Nessun iscritto trovato."
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { messaggio = "Impossibile caricare gli iscritti. Riprova."
            } finally { inCorso = false }
        }
    }

    fun scegliIscritto(id: Long) {
        if (inCorso) return
        inCorso = true
        viewModelScope.launch {
            try { utenteScelto = repository.utente(id)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { messaggio = "Impossibile selezionare l'iscritto."
            } finally { inCorso = false }
        }
    }

    fun chiudiCaricamento() { utenteScelto = null }
    fun certificatoSalvato() {
        utenteScelto = null
        cerca(filtro)
    }
}
