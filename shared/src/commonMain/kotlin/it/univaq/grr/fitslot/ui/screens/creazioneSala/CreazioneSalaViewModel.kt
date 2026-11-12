package it.univaq.grr.fitslot.ui.screens.creazioneSala
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
/** Gestisce modulo ed elenco senza accedere direttamente al database. */
class CreazioneSalaViewModel(private val salaRepository: SalaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CreazioneSalaUiState())
    val uiState = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            try { salaRepository.osservaSale().collect { sale -> _uiState.update { it.copy(elenco = sale, caricamento = false) } } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(caricamento = false, errore = "Impossibile caricare le sale. Torna alla Home e riprova.") } }
        }
    }
    private fun aggiorna(modifica: (CreazioneSalaUiState) -> CreazioneSalaUiState) {
        if (_uiState.value.salvataggioInCorso) return
        _uiState.update { modifica(it).copy(errore = null, conferma = null) }
    }
    fun aggiornaNome(nome: String) = aggiorna { it.copy(nome = nome) }
    fun aggiornaCapienza(capienza: String) = aggiorna { it.copy(capienza = capienza) }
    fun modifica(sala: Sala) = aggiorna { it.copy(idModifica = sala.id, nome = sala.nome, capienza = sala.capienza.toString()) }
    fun annullaModifica() = aggiorna { it.copy(idModifica = null, nome = "", capienza = "") }
    fun richiediEliminazione(sala: Sala) = aggiorna { it.copy(daEliminare = sala) }
    fun annullaEliminazione() = aggiorna { it.copy(daEliminare = null) }
    fun salva() {
        val stato = _uiState.value
        if (stato.salvataggioInCorso) return
        val capienza = stato.capienza.trim().toIntOrNull()
        val errore = when {
            stato.nome.isBlank() -> "Inserisci il nome della sala."
            capienza == null || capienza <= 0 -> "Inserisci una capienza intera maggiore di zero."
            else -> null
        }
        if (errore != null) { _uiState.update { it.copy(errore = errore, conferma = null) }; return }
        esegui {
            val sala = Sala(stato.idModifica ?: 0, stato.nome.trim(), capienza!!)
            if (stato.idModifica == null) salaRepository.inserisci(sala) else salaRepository.modifica(sala)
            _uiState.update { it.copy(idModifica = null, nome = "", capienza = "", conferma =
                if (stato.idModifica == null) "Sala creata correttamente." else "Sala modificata correttamente.") }
        }
    }
    fun confermaEliminazione() {
        if (_uiState.value.salvataggioInCorso) return
        val sala = _uiState.value.daEliminare ?: return
        esegui {
            salaRepository.elimina(sala.id)
            _uiState.update {
                val stato = if (it.idModifica == sala.id) it.copy(idModifica = null, nome = "", capienza = "") else it
                stato.copy(daEliminare = null, conferma = "Sala eliminata correttamente.")
            }
        }
    }
    private fun esegui(operazione: suspend () -> Unit) {
        _uiState.update { it.copy(salvataggioInCorso = true, errore = null, conferma = null) }
        viewModelScope.launch {
            try { operazione() }
            catch (e: CancellationException) { throw e }
            catch (e: IllegalArgumentException) { mostraErrore(e.message) }
            catch (e: IllegalStateException) { mostraErrore(e.message) }
            catch (e: Exception) { mostraErrore("Operazione non riuscita. Riprova.") }
            finally { _uiState.update { it.copy(salvataggioInCorso = false) } }
        }
    }
    private fun mostraErrore(messaggio: String?) {
        _uiState.update { it.copy(errore = messaggio ?: "Operazione non riuscita.", daEliminare = null) }
    }
}
