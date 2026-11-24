package it.univaq.grr.fitslot.ui.screens.certificatoMedico

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.utils.MediaStorage
import it.univaq.grr.fitslot.utils.dataScadenza
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import it.univaq.grr.fitslot.utils.dataOggi
import it.univaq.grr.fitslot.utils.aggiungiGiorni

fun certificatoInScadenza(scadenza: String?, oggi: String): Boolean =
    scadenza != null && scadenza >= oggi && scadenza <= aggiungiGiorni(oggi, 20)

class CertificatoMedicoViewModel(private val repository: CertificatoMedicoRepository) : ViewModel() {
    var presente by mutableStateOf(false)
        private set
    var scadenza by mutableStateOf<String?>(null)
        private set
    var documento by mutableStateOf<ByteArray?>(null)
        private set
    var percorso by mutableStateOf("")
        private set
    var messaggio by mutableStateOf("")
        private set
    var inCorso by mutableStateOf(false)
        private set

    fun carica(utente: User) {
        if (inCorso) return
        inCorso = true
        presente = false
        documento = null
        messaggio = ""
        viewModelScope.launch {
            try {
                val certificato = repository.getCertificatoMedicoByUser(utente)
                presente = certificato != null
                scadenza = dataScadenza(certificato?.scadenza)
                percorso = certificato?.path.orEmpty()
                if (certificato != null) {
                    documento = MediaStorage().retrieveImage(certificato.path).getOrThrow()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messaggio = "Impossibile leggere il certificato. Riprova o contatta la reception."
            } finally {
                inCorso = false
            }
        }
    }
    fun apriDocumento() {
        viewModelScope.launch {
            if (MediaStorage().apriDocumento(percorso).isFailure) messaggio = "Impossibile aprire il documento con l'app predefinita."
        }
    }
}
