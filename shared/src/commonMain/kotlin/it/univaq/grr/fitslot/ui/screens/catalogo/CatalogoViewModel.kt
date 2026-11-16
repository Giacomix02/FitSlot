package it.univaq.grr.fitslot.ui.screens.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.PianoAPunti
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import it.univaq.grr.fitslot.utils.formattaPrezzo
import it.univaq.grr.fitslot.utils.prezzoInCentesimi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Gestisce lo stesso flusso di lavoro per le due tipologie di piano. */
class CatalogoViewModel(
    val tipo: TipoCatalogo,
    private val repository: CatalogoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carica()
    }

    private fun carica() {
        viewModelScope.launch {
            try {
                if (tipo == TipoCatalogo.ABBONAMENTI) {
                    repository.osservaAbbonamenti().collect { abbonamenti ->
                        val elenco = mutableListOf<VoceCatalogo>()
                        for (abbonamento in abbonamenti) {
                            elenco.add(VoceCatalogo(abbonamento.id, abbonamento.nome,
                                abbonamento.prezzoCentesimi, abbonamento.durataGiorni))
                        }
                        _uiState.update { it.copy(elenco = elenco, caricamento = false) }
                    }
                } else {
                    repository.osservaPianiAPunti().collect { piani ->
                        val elenco = mutableListOf<VoceCatalogo>()
                        for (piano in piani) {
                            elenco.add(VoceCatalogo(piano.id, piano.nome,
                                piano.prezzoCentesimi, piano.ingressiInclusi))
                        }
                        _uiState.update { it.copy(elenco = elenco, caricamento = false) }
                    }
                }
            } catch (e: CancellationException) {
                // La chiusura della schermata non è un errore da mostrare.
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(caricamento = false, errore = "Impossibile caricare i piani. Torna alla Home e riprova.") }
            }
        }
    }

    // Ogni azione prepara il nuovo stato della schermata.
    private fun aggiorna(nuovoStato: CatalogoUiState) {
        if (_uiState.value.operazioneInCorso) return
        _uiState.value = nuovoStato.copy(errore = null, conferma = null)
    }

    fun aggiornaNome(valore: String) {
        val stato = _uiState.value
        aggiorna(stato.copy(nome = valore))
    }

    fun aggiornaPrezzo(valore: String) {
        val stato = _uiState.value
        aggiorna(stato.copy(prezzo = valore))
    }

    fun aggiornaQuantita(valore: String) {
        val stato = _uiState.value
        aggiorna(stato.copy(quantita = valore))
    }

    fun modifica(voce: VoceCatalogo) {
        val stato = _uiState.value
        aggiorna(stato.copy(idModifica = voce.id, nome = voce.nome, prezzo = formattaPrezzo(voce.prezzoCentesimi), quantita = voce.quantita.toString()))
    }

    fun nuovo() {
        val stato = _uiState.value
        aggiorna(stato.copy(idModifica = null, nome = "", prezzo = "", quantita = ""))
    }

    fun richiediEliminazione(voce: VoceCatalogo) {
        val stato = _uiState.value
        aggiorna(stato.copy(daEliminare = voce))
    }

    fun annullaEliminazione() {
        val stato = _uiState.value
        aggiorna(stato.copy(daEliminare = null))
    }

    fun salva() {
        val stato = _uiState.value
        if (stato.operazioneInCorso) return
        val prezzo = prezzoInCentesimi(stato.prezzo)
        val quantita = stato.quantita.trim().toIntOrNull()
        if (stato.nome.isBlank()) {
            mostraErrore("Inserisci il nome del piano.")
            return
        }
        if (prezzo == null) {
            mostraErrore("Inserisci un prezzo valido, con al massimo due decimali.")
            return
        }
        if (quantita == null || quantita <= 0) {
            if (tipo == TipoCatalogo.ABBONAMENTI) {
                mostraErrore("Inserisci una durata in giorni maggiore di zero.")
            } else {
                mostraErrore("Inserisci un numero di ingressi maggiore di zero.")
            }
            return
        }
        _uiState.update { it.copy(operazioneInCorso = true, errore = null, conferma = null) }
        viewModelScope.launch {
            try {
                if (tipo == TipoCatalogo.ABBONAMENTI) {
                    repository.salvaAbbonamento(Abbonamento(stato.idModifica ?: 0, stato.nome.trim(), prezzo, quantita))
                } else {
                    repository.salvaPianoAPunti(PianoAPunti(stato.idModifica ?: 0, stato.nome.trim(), prezzo, quantita))
                }
                val messaggio = if (stato.idModifica == null) {
                    "Piano creato e disponibile per la sottoscrizione."
                } else {
                    "Modifiche salvate correttamente."
                }
                _uiState.update {
                    it.copy(idModifica = null, nome = "", prezzo = "", quantita = "", conferma = messaggio)
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
                _uiState.update { it.copy(operazioneInCorso = false) }
            }
        }
    }

    fun confermaEliminazione() {
        if (_uiState.value.operazioneInCorso) return
        val voce = _uiState.value.daEliminare ?: return
        _uiState.update { it.copy(operazioneInCorso = true, errore = null, conferma = null) }
        viewModelScope.launch {
            try {
                if (tipo == TipoCatalogo.ABBONAMENTI) {
                    repository.eliminaAbbonamento(voce.id)
                } else {
                    repository.eliminaPianoAPunti(voce.id)
                }
                _uiState.update {
                    val nuovoStato = if (it.idModifica == voce.id)
                        it.copy(idModifica = null, nome = "", prezzo = "", quantita = "") else it
                    nuovoStato.copy(daEliminare = null, conferma = "Piano eliminato correttamente.")
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
                _uiState.update { it.copy(operazioneInCorso = false) }
            }
        }
    }

    private fun mostraErrore(messaggio: String?) {
        _uiState.update { it.copy(errore = messaggio ?: "Operazione non riuscita.", conferma = null, daEliminare = null) }
    }
}
