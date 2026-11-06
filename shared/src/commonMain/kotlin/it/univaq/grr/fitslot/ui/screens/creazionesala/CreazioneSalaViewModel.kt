package it.univaq.grr.fitslot.ui.screens.creazionesala

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Gestisce i campi e il salvataggio della nuova sala.
 * Accede ai dati attraverso il repository, senza conoscere Room. */

class CreazioneSalaViewModel(
    private val salaRepository: SalaRepository
) : ViewModel() {

    // Solo il ViewModel può modificare lo stato.
    private val _uiState = MutableStateFlow(CreazioneSalaUiState())

    // La schermata può osservare lo stato, senza modificarlo direttamente.
    val uiState = _uiState.asStateFlow()

    fun aggiornaNome(nome: String) {
        if (_uiState.value.salvataggioInCorso) return

        _uiState.update {
            it.copy(nome = nome, errore = null, salaCreata = false)
        }
    }

    fun aggiornaCapienza(capienza: String) {
        if (_uiState.value.salvataggioInCorso) return

        _uiState.update {
            it.copy(capienza = capienza, errore = null, salaCreata = false)
        }
    }

    fun salva() {
        val stato = _uiState.value

        // Evita richieste duplicate durante il salvataggio
        // o dopo il successo, finché i campi non vengono modificati.
        if (stato.salvataggioInCorso || stato.salaCreata) return

        val nome = stato.nome.trim()
        val capienza = stato.capienza.trim().toIntOrNull()

        if (nome.isBlank()) {
            _uiState.update {
                it.copy(errore = "Inserisci il nome della sala.")
            }
            return
        }

        if (capienza == null || capienza <= 0) {
            _uiState.update {
                it.copy(errore = "Inserisci una capienza intera maggiore di zero.")
            }
            return
        }

        _uiState.update {
            it.copy(salvataggioInCorso = true, errore = null)
        }

        // La coroutine permette di chiamare la funzione suspend del repository.
        viewModelScope.launch {
            try {
                val sala = Sala(nome = nome, capienza = capienza)
                salaRepository.inserisci(sala)

                // Mostra la conferma soltanto dopo l'inserimento riuscito.
                _uiState.update {
                    it.copy(salaCreata = true)
                }
            } catch (e: CancellationException) {
                // La cancellazione della coroutine deve propagarsi.
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errore = "Impossibile salvare la sala. Riprova.")
                }
            } finally {
                _uiState.update {
                    it.copy(salvataggioInCorso = false)
                }
            }
        }
    }
}