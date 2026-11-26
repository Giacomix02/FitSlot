package it.univaq.grr.fitslot.ui.screens.LogIngresso

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.data.exceptions.CertificatoMedicoNonTrovatoException
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.LogAccesso
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.domain.repository.LogAccessoRepository
import it.univaq.grr.fitslot.domain.repository.UserRepository
import it.univaq.grr.fitslot.utils.MediaStorage
import it.univaq.grr.fitslot.utils.convertMillisToDate
import it.univaq.grr.fitslot.utils.isMillisBeforeToday
import it.univaq.grr.fitslot.utils.toImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogAccessoScreenState(
    val showCertificato: Boolean = false,
    val isCertificatoValido: Boolean = false,
    val certificatoImage: ImageBitmap? = null,
    val certificatoScadenza: String? = null
)

class LogAccessoViewModel(
    private val mediaStorage: MediaStorage,
    private val logAccessoRepository: LogAccessoRepository,
    private val userRepository: UserRepository,
    private val certificatoMedicoRepository: CertificatoMedicoRepository
): ViewModel() {

    val selectedUser = mutableStateOf<User?>(null)

    var error by mutableStateOf(false)

    val iscritti = mutableStateListOf<User>()

    var textErrore by mutableStateOf("Azione non consentita")

    val uiState: StateFlow<LogAccessoScreenState> field = MutableStateFlow(LogAccessoScreenState())

    init {
        getAllIscritti()
    }

    private fun getAllIscritti() {
        viewModelScope.launch {
            iscritti.clear()
            iscritti.addAll(userRepository.getAllIscritti())
        }
    }



    fun onMatricolaChanged() {
        selectedUser.value = null
        error = false
        textErrore = "Azione non consentita"
    }

    fun onUserSelected(user: User) {
        selectedUser.value = user
        error = false
        textErrore = "Azione non consentita"
    }

    fun onLog(onSuccess: () -> Unit) {
        val user = selectedUser.value

        if (user == null) {
            error = true
            textErrore = "Seleziona una matricola"
            return
        }

        viewModelScope.launch {
            val result = logAccessoRepository.insertAccesso(
                LogAccesso(
                    user = user,
                    timestamp = System.currentTimeMillis()
                )
            )

            if (result > 0) {
                onSuccess()
            } else {
                error = true
                textErrore = "Errore durante la registrazione"
            }
        }
    }

    fun setShowCertificato(show: Boolean) {
        uiState.update { it.copy(showCertificato = show) }
    }

    fun bringUpCertificato() = viewModelScope.launch {
        val user = selectedUser.value
        if (user == null) {
            error = true
            textErrore = "Nessun utente selezionato"
            uiState.update { it.copy(showCertificato = false, isCertificatoValido = false) }
            return@launch
        }

        var certificato: CertificatoMedico? = null
        try {
          certificato = certificatoMedicoRepository.getCertificatoMedicoByUser(user)
        } catch (_: CertificatoMedicoNonTrovatoException) {
            error = true
            textErrore = "Nessun certificato medico trovato per l'utente selezionato"
            return@launch
        }
        mediaStorage.retrieveImage(certificato.path).fold(
            onSuccess = { image ->
                error = false
                uiState.update { it.copy(
                    certificatoImage = image.toImageBitmap(),
                    certificatoScadenza =  convertMillisToDate(certificato.scadenza),
                    isCertificatoValido = certificato.scadenza.isMillisBeforeToday(),
                    showCertificato = true
                )
                }
            },
            onFailure = {
                error=true
                textErrore = it.localizedMessage
                uiState.update { it.copy() } }

        )
    }


}