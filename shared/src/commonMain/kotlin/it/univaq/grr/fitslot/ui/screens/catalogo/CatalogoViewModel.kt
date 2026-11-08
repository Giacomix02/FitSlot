package it.univaq.grr.fitslot.ui.screens.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.*
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Gestisce lo stesso flusso di lavoro per le due tipologie di piano. */
class CatalogoViewModel(
    val tipo: TipoCatalogo,
    private val repository: CatalogoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState = _uiState.asStateFlow()

    init { carica() }

    private fun carica() {
        viewModelScope.launch {
            try {
                val elenco = if (tipo == TipoCatalogo.ABBONAMENTI) {
                    repository.osservaAbbonamenti().map { lista ->
                        lista.map { VoceCatalogo(it.id, it.nome, it.prezzoCentesimi, it.durataGiorni) }
                    }
                } else {
                    repository.osservaPianiAPunti().map { lista ->
                        lista.map { VoceCatalogo(it.id, it.nome, it.prezzoCentesimi, it.ingressiInclusi) }
                    }
                }
                elenco.collect { lista -> _uiState.update { it.copy(elenco = lista, caricamento = false) } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(caricamento = false, errore = "Impossibile caricare i piani. Torna alla Home e riprova.") }
            }
        }
    }

    fun aggiornaNome(valore: String) = aggiorna { it.copy(nome = valore) }
    fun aggiornaPrezzo(valore: String) = aggiorna { it.copy(prezzo = valore) }
    fun aggiornaQuantita(valore: String) = aggiorna { it.copy(quantita = valore) }

    private fun aggiorna(modifica: (CatalogoUiState) -> CatalogoUiState) {
        if (_uiState.value.operazioneInCorso) return
        _uiState.update { modifica(it).copy(errore = null, conferma = null) }
    }

    fun modifica(voce: VoceCatalogo) = aggiorna {
        it.copy(idModifica = voce.id, nome = voce.nome,
            prezzo = formattaPrezzo(voce.prezzoCentesimi), quantita = voce.quantita.toString())
    }

    fun nuovo() = aggiorna { it.copy(idModifica = null, nome = "", prezzo = "", quantita = "") }
    fun richiediEliminazione(voce: VoceCatalogo) = aggiorna { it.copy(daEliminare = voce) }
    fun annullaEliminazione() = aggiorna { it.copy(daEliminare = null) }

    fun salva() {
        val stato = _uiState.value
        if (stato.operazioneInCorso) return
        val prezzo = prezzoInCentesimi(stato.prezzo)
        val quantita = stato.quantita.trim().toIntOrNull()
        val errore = when {
            stato.nome.isBlank() -> "Inserisci il nome del piano."
            prezzo == null -> "Inserisci un prezzo valido, con al massimo due decimali."
            quantita == null || quantita <= 0 -> if (tipo == TipoCatalogo.ABBONAMENTI)
                "Inserisci una durata in giorni maggiore di zero." else "Inserisci un numero di ingressi maggiore di zero."
            else -> null
        }
        if (errore != null) { _uiState.update { it.copy(errore = errore, conferma = null) }; return }
        esegui {
            if (tipo == TipoCatalogo.ABBONAMENTI) {
                repository.salvaAbbonamento(Abbonamento(stato.idModifica ?: 0, stato.nome.trim(), prezzo!!, quantita!!))
            } else {
                repository.salvaPianoAPunti(PianoAPunti(stato.idModifica ?: 0, stato.nome.trim(), prezzo!!, quantita!!))
            }
            _uiState.update {
                it.copy(idModifica = null, nome = "", prezzo = "", quantita = "",
                    conferma = if (stato.idModifica == null) "Piano creato e disponibile per la sottoscrizione." else "Modifiche salvate correttamente.")
            }
        }
    }

    fun confermaEliminazione() {
        if (_uiState.value.operazioneInCorso) return
        val voce = _uiState.value.daEliminare ?: return
        esegui {
            if (tipo == TipoCatalogo.ABBONAMENTI) repository.eliminaAbbonamento(voce.id)
            else repository.eliminaPianoAPunti(voce.id)
            _uiState.update {
                val nuovoStato = if (it.idModifica == voce.id)
                    it.copy(idModifica = null, nome = "", prezzo = "", quantita = "") else it
                nuovoStato.copy(daEliminare = null, conferma = "Piano eliminato correttamente.")
            }
        }
    }

    private fun esegui(operazione: suspend () -> Unit) {
        _uiState.update { it.copy(operazioneInCorso = true, errore = null, conferma = null) }
        viewModelScope.launch {
            try { operazione() }
            catch (e: CancellationException) { throw e }
            catch (e: IllegalArgumentException) { mostraErrore(e.message) }
            catch (e: IllegalStateException) { mostraErrore(e.message) }
            catch (e: Exception) { mostraErrore("Operazione non riuscita. Riprova.") }
            finally { _uiState.update { it.copy(operazioneInCorso = false) } }
        }
    }

    private fun mostraErrore(messaggio: String?) {
        _uiState.update { it.copy(errore = messaggio ?: "Operazione non riuscita.", daEliminare = null) }
    }
}
