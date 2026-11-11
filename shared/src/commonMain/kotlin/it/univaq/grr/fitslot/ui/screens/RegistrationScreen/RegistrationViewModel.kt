package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.SQLiteException
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class RegistrationField(
    val key: String,
    val label: String,
    val errorMessage: String,
    val required: Boolean = true
)

enum class errors(val message: String) {
    CAMPI_VUOTI("Controlla i dati inseriti"),
    ERRORE_REGISTRAZIONE("Errore durante la registrazione"),
    MAIL_IN_USO("Email già in uso")
}

class RegistrationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {


    private val emailRegex = Regex(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )

    private val phoneRegex = Regex(
        "^\\+?[0-9\\s-]{6,15}$"
    )

    private fun validazioneMail(email: String): Boolean {
        return emailRegex.matches(email)
    }

    private fun validazioneTelefono(phone: String): Boolean {
        return phoneRegex.matches(phone)
    }

    val fields = listOf(
        RegistrationField("email", "Email", "Inserisci l'Email"),
        RegistrationField("password", "Password", "Inserisci la Password"),
        RegistrationField("username", "Nome utente", "Inserisci il nome utente"),
        RegistrationField("nome", "Nome", "Inserisci il Nome"),
        RegistrationField("cognome", "Cognome", "Inserisci il Cognome"),
        RegistrationField("telefono", "Telefono", "Inserisci il Telefono"),
        RegistrationField("indirizzo", "Indirizzo", "Inserisci l'Indirizzo"),
        RegistrationField("citta", "Città", "Inserisci la Città"),
        RegistrationField("emailContatto", "Email di contatto", "Inserisci l'Email di contatto")
    )

    private val values = mutableStateMapOf<String, String>()

    private var dateNascitaMillis by mutableStateOf<Long?>(null)

    var registrationError = mutableStateOf(false)
        private set

    var registrationErrorText = mutableStateOf("")

    var submitted = mutableStateOf(false)
        private set

    fun valueOf(key: String): String {
        return values[key] ?: ""
    }

    fun onValueChange(key: String, value: String) {
        values[key] = value
        registrationError.value = false
    }

    fun onDateChange(dateMillis: Long?) {
        dateNascitaMillis = dateMillis
        registrationError.value = false
    }

    fun dateErrorMessage(): String? {
        return if (submitted.value && dateNascitaMillis == null) {
            "Inserisci la data di nascita"
        } else {
            null
        }
    }

    fun errorOf(field: RegistrationField): String? {
        if (!submitted.value || !field.required) {
            return null
        }

        val value = valueOf(field.key).trim()

        // 1. Controllo campo vuoto
        if (value.isBlank()) {
            return field.errorMessage
        }

        // 2. Controllo Email (copre sia email che eventuale emailContatto)
        if (field.key == "email" || field.key == "emailContatto") {
            if (!validazioneMail(value)) {
                return "Formato email non valido"
            }
        }

        // 3. Controllo Telefono
        if (field.key == "telefono") {
            if (!validazioneTelefono(value)) {
                return "Inserisci solo cifre valide per il telefono"
            }
        }

        return null
    }

    fun onRegisterClick(onSuccess: () -> Unit) {
        submitted.value = true

        val selectedDateMillis = dateNascitaMillis

        val hasValidationErrors = fields.any { errorOf(it) != null }
        if (hasValidationErrors || selectedDateMillis == null) {
            registrationErrorText.value = errors.CAMPI_VUOTI.message
            registrationError.value = true
            return
        }

        val user = User(
            email = valueOf("email"),
            password = valueOf("password"),
            username = valueOf("username"),
            nome = valueOf("nome"),
            cognome = valueOf("cognome"),
            dataNascita = selectedDateMillis.toString(),
            telefono = valueOf("telefono"),
            indirizzo = valueOf("indirizzo"),
            citta = valueOf("citta"),
            emailContatto = valueOf("emailContatto"),
            ruolo = Ruolo.ISCRITTO
        )

        viewModelScope.launch {
            try {
                val result = userRepository.userRegistration(user)
                onSuccess()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                print("Eccezione Registrazione" + e.message)
                if (e is SQLiteException) {
                    registrationErrorText.value = errors.MAIL_IN_USO.message
                    registrationError.value = true
                } else {
                    registrationErrorText.value = errors.ERRORE_REGISTRAZIONE.message
                    registrationError.value = true
                }
            }
        }
    }
}
