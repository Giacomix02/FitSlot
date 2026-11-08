package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Configurazione locale disponibile soltanto prima di creare il primo utente. */
class ConfigurazioneGestoreViewModel(private val repository: UserRepository,
                                    private val sessione: SessioneUtente) : ViewModel() {
    var nome by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confermaPassword by mutableStateOf("")
    var inCorso by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set

    fun crea(onSuccess: () -> Unit) {
        if (inCorso) return
        if (password != confermaPassword) { errore = "Le password non coincidono."; return }
        inCorso = true
        errore = null
        viewModelScope.launch {
            try {
                sessione.accedi(repository.creaPrimoGestore(email, password, nome))
                password = ""
                confermaPassword = ""
                onSuccess()
            } catch (e: CancellationException) { throw e }
            catch (e: IllegalArgumentException) { errore = e.message }
            catch (e: IllegalStateException) { errore = e.message }
            catch (e: Exception) { errore = "Configurazione non riuscita. Riprova." }
            finally { inCorso = false }
        }
    }
}

@Composable
fun ConfigurazioneGestoreScreen(vm: ConfigurazioneGestoreViewModel, onSuccess: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Configura il primo gestore", style = MaterialTheme.typography.headlineMedium)
        Text("Non ci sono utenti nel database. Scegli le credenziali per gestire questa installazione di FitSlot.")
        OutlinedTextField(vm.nome, { vm.nome = it }, label = { Text("Nome") }, singleLine = true, enabled = !vm.inCorso)
        OutlinedTextField(vm.email, { vm.email = it }, label = { Text("Email") }, singleLine = true, enabled = !vm.inCorso)
        OutlinedTextField(vm.password, { vm.password = it }, label = { Text("Password (almeno 8 caratteri)") },
            singleLine = true, enabled = !vm.inCorso, visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(vm.confermaPassword, { vm.confermaPassword = it }, label = { Text("Conferma password") },
            singleLine = true, enabled = !vm.inCorso, visualTransformation = PasswordVisualTransformation())
        Button(onClick = { vm.crea(onSuccess) }, enabled = !vm.inCorso) {
            Text(if (vm.inCorso) "Configurazione..." else "Crea gestore")
        }
        vm.errore?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
