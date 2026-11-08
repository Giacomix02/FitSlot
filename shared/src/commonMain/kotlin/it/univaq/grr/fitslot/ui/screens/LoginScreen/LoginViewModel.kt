package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Conserva nella sessione l'utente restituito dal login del team. */
class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessione: SessioneUtente
) : ViewModel() {
    var emailInput by mutableStateOf("")
        private set
    var passwordInput by mutableStateOf("")
        private set
    var loginError by mutableStateOf(false)
        private set
    var loginInCorso by mutableStateOf(false)
        private set
    var erroreSistema by mutableStateOf<String?>(null)
        private set

    fun onEmailInputChange(newEmail: String) { if (!loginInCorso) emailInput = newEmail }
    fun onPasswordInputChange(newPassword: String) { if (!loginInCorso) passwordInput = newPassword }

    fun onLoginClick(onSuccess: () -> Unit) {
        if (loginInCorso) return
        loginInCorso = true
        loginError = false
        erroreSistema = null
        viewModelScope.launch {
            try {
                // Il calcolo dell'hash non deve bloccare l'interfaccia.
                val user = withContext(Dispatchers.Default) {
                    userRepository.userLogin(emailInput.trim(), passwordInput)
                }
                if (user == null) loginError = true
                else {
                    sessione.accedi(user)
                    passwordInput = ""
                    onSuccess()
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { erroreSistema = "Accesso non riuscito. Riprova." }
            finally { loginInCorso = false }
        }
    }
}
