package it.univaq.grr.fitslot.ui.screens.creazioneSala

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Gestisce modulo ed elenco senza accedere direttamente al database. */
class CreazioneSalaViewModel(private val salaRepository: SalaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CreazioneSalaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                salaRepository.osservaSale().collect { sale ->
                    _uiState.update { it.copy(elenco = sale, caricamento = false) }
                }
            } catch (e: CancellationException) {
                // La chiusura della schermata non è un errore da mostrare.
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(caricamento = false, errore = "Impossibile caricare le sale. Torna alla Home e riprova.")
                }
            }
        }
    }

    // Ogni azione prepara il nuovo stato della schermata.
    private fun aggiorna(nuovoStato: CreazioneSalaUiState) {
        if (_uiState.value.salvataggioInCorso) return
        _uiState.value = nuovoStato.copy(errore = null, conferma = null)
    }

    fun aggiornaNome(nome: String) {
        val stato = _uiState.value
        aggiorna(stato.copy(nome = nome))
    }

    fun aggiornaCapienza(capienza: String) {
        val stato = _uiState.value
        aggiorna(stato.copy(capienza = capienza))
    }

    fun modifica(sala: Sala) {
        val stato = _uiState.value
        aggiorna(stato.copy(idModifica = sala.id, nome = sala.nome, capienza = sala.capienza.toString()))
    }

    fun annullaModifica() {
        val stato = _uiState.value
        aggiorna(stato.copy(idModifica = null, nome = "", capienza = ""))
    }

    fun richiediEliminazione(sala: Sala) {
        val stato = _uiState.value
        aggiorna(stato.copy(daEliminare = sala))
    }

    fun annullaEliminazione() {
        val stato = _uiState.value
        aggiorna(stato.copy(daEliminare = null))
    }

    fun salva() {
        val stato = _uiState.value
        if (stato.salvataggioInCorso) return
        val capienza = stato.capienza.trim().toIntOrNull()
        if (stato.nome.isBlank()) {
            mostraErrore("Inserisci il nome della sala.")
            return
        }
        if (capienza == null || capienza <= 0) {
            mostraErrore("Inserisci una capienza intera maggiore di zero.")
            return
        }
        _uiState.update { it.copy(salvataggioInCorso = true, errore = null, conferma = null) }
        viewModelScope.launch {
            try {
                val sala = Sala(id = stato.idModifica ?: 0, nome = stato.nome.trim(), capienza = capienza)
                if (stato.idModifica == null) {
                    salaRepository.inserisci(sala)
                } else {
                    salaRepository.modifica(sala)
                }
                val messaggio = if (stato.idModifica == null) {
                    "Sala creata correttamente."
                } else {
                    "Sala modificata correttamente."
                }
                _uiState.update {
                    it.copy(idModifica = null, nome = "", capienza = "", conferma = messaggio)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                mostraErrore(e.message)
            } catch (e: IllegalStateException) {
                mostraErrore(e.message)
            } catch (e: Exception) {
                mostraErrore("Operazione non riuscita. Riprova.")
            } finally {
                _uiState.update { it.copy(salvataggioInCorso = false) }
            }
        }
    }

    fun confermaEliminazione() {
        if (_uiState.value.salvataggioInCorso) return
        val sala = _uiState.value.daEliminare ?: return
        _uiState.update { it.copy(salvataggioInCorso = true, errore = null, conferma = null) }
        viewModelScope.launch {
            try {
                salaRepository.elimina(sala.id)
                _uiState.update {
                    val stato = if (it.idModifica == sala.id) it.copy(idModifica = null, nome = "", capienza = "") else it
                    stato.copy(daEliminare = null, conferma = "Sala eliminata correttamente.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                mostraErrore(e.message)
            } catch (e: IllegalStateException) {
                mostraErrore(e.message)
            } catch (e: Exception) {
                mostraErrore("Operazione non riuscita. Riprova.")
            } finally {
                _uiState.update { it.copy(salvataggioInCorso = false) }
            }
        }
    }

    private fun mostraErrore(messaggio: String?) {
        _uiState.update { it.copy(errore = messaggio ?: "Operazione non riuscita.", conferma = null, daEliminare = null) }
    }
}
