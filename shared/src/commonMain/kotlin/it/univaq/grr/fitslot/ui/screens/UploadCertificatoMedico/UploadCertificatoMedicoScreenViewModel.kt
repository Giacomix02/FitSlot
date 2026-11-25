package it.univaq.grr.fitslot.ui.screens.UploadCertificatoMedico

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.utils.MediaStorage
import kotlinx.coroutines.launch

class UploadCertificatoMedicoScreenViewModel(
    private val certificatoMedicoRepository: CertificatoMedicoRepository
): ViewModel() {

    private var scadenzaCertificatoSelected by mutableStateOf<Long?>(null)

    var error = mutableStateOf(false)

    var selectedFileName = mutableStateOf<String?>(null)

    private var certificatoFileSelected: PlatformFile? = null

    fun resetSelections() {
        certificatoFileSelected = null
        selectedFileName.value = null
        scadenzaCertificatoSelected = null
        error.value = false
    }

     fun uploadCertificato(onSuccess: () -> Unit){

         if(scadenzaCertificatoSelected == null || selectedFileName.value == null || certificatoFileSelected == null){
             error.value = true
             return
         }

         try {
             viewModelScope.launch {

                 val bytes = certificatoFileSelected!!.readBytes()
                 val estensione = certificatoFileSelected!!.name.substringAfterLast('.')

                 MediaStorage().storeImage(bytes, estensione).fold(
                     onSuccess = { path ->
                         certificatoMedicoRepository.addCertificatoMedico(
                             CertificatoMedico(
                                 utente = user,
                                 path = path,
                                 scadenza = scadenzaCertificatoSelected!!
                             )
                         )
                         onSuccess()
                     },
                     onFailure = {
                         error.value = true
                     }
                 )


             }

         }catch (e: Exception){
             error.value = true
         }


    }

    fun onDateChange(dateMillis: Long?) {
        scadenzaCertificatoSelected = dateMillis
        error.value = false
    }

    fun onFileChange(file: PlatformFile?) {
        certificatoFileSelected = file
        selectedFileName.value = file?.name
        error.value = false
    }



}

//TODO togliere l'utente mock
 val user = User(
    id = 16,
    email = "mock",
    password = "mock",
    username = "mock",
    nome =  "mock",
    cognome = "mock",
    dataNascita = "mock",
    telefono = "mock",
    indirizzo = "mock",
    citta = "mock",
    emailContatto = "mock",
    ruolo = Ruolo.ISCRITTO,
)