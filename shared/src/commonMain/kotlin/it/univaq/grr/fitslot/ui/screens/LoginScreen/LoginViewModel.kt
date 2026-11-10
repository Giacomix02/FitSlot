package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.data.local.dao.UserDao
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.domain.repository.UserRepository
import kotlinx.coroutines.launch


class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    var emailInput by mutableStateOf("")
        private set

    var passwordInput by mutableStateOf("")
        private set

    var loginError by mutableStateOf(false)
        private set

    fun onEmailInputChange(newEmail: String) {
        emailInput = newEmail
    }

    fun onPasswordInputChange(newPassword: String) {
        passwordInput = newPassword
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        if(emailInput.isEmpty() || passwordInput.isEmpty()) {
            loginError = true
            return
        }

        viewModelScope.launch {     //avvia una coroutine per eseguire il login in background in modo tale che l'azione non sia bloccante
            val user = userRepository.userLogin(
                email = emailInput,
                password = passwordInput
            )

            if (user != null) {
                loginError = false
                onSuccess()
            } else {
                loginError = true
            }
        }
    }
}