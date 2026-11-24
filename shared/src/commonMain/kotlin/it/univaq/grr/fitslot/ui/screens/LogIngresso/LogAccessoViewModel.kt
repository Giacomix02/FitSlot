package it.univaq.grr.fitslot.ui.screens.LogIngresso

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.models.LogAccesso
import it.univaq.grr.fitslot.domain.repository.LogAccessoRepository
import it.univaq.grr.fitslot.domain.repository.UserRepository
import kotlinx.coroutines.launch

class LogAccessoViewModel(
    private val logAccessoRepository: LogAccessoRepository,
    private val userRepository: UserRepository
): ViewModel() {

    val selectedUser = mutableStateOf<User?>(null)

    var error by mutableStateOf(false)

    val iscritti = mutableStateListOf<User>()

    var textErrore by mutableStateOf("Azione non consentita")

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



}