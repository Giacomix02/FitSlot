package it.univaq.grr.fitslot.ui.screens.UploadCertificatoMedico

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import it.univaq.grr.fitslot.domain.PermessiReception
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.utils.MediaStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class UploadCertificatoMedicoScreenViewModel(private val repository: CertificatoMedicoRepository) : ViewModel() {
    private var utente: User? = null
    private var scadenza: Long? = null
    private var file: PlatformFile? = null
    var error = mutableStateOf(false)
    var selectedFileName = mutableStateOf<String?>(null)
    var inCorso by mutableStateOf(false)
        private set

    fun selezionaUtente(valore: User?) { resetSelections(); utente = valore }
    fun resetSelections() {
        file = null
        scadenza = null
        selectedFileName.value = null
        error.value = false
    }

    fun uploadCertificato(onSuccess: () -> Unit) {
        if (inCorso) return
        val iscritto = utente
        val data = scadenza
        val documento = file
        if (iscritto == null || data == null || documento == null || !PermessiReception.puoControllare()) {
            error.value = true
            return
        }
        inCorso = true
        viewModelScope.launch {
            try {
                val percorso = MediaStorage().storeImage(documento.readBytes(), documento.name.substringAfterLast('.')).getOrThrow()
                repository.addCertificatoMedico(CertificatoMedico(utente = iscritto, path = percorso, scadenza = data.toString()))
                onSuccess()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { error.value = true
            } finally { inCorso = false }
        }
    }

    fun onDateChange(valore: Long?) { if (!inCorso) { scadenza = valore; error.value = false } }
    fun onFileChange(valore: PlatformFile?) { if (!inCorso) { file = valore; selectedFileName.value = valore?.name; error.value = false } }
}
