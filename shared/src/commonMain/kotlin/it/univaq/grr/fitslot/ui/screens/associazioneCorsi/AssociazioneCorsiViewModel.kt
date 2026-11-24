package it.univaq.grr.fitslot.ui.screens.associazioneCorsi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AssociazioneCorsiViewModel(
    private val catalogo: CatalogoRepository
) : ViewModel() {
    var corsi by mutableStateOf<List<Corso>>(emptyList())
        private set
    var abbonamentoScelto by mutableStateOf<Abbonamento?>(null)
        private set
    var corsiSelezionati by mutableStateOf<Set<Long>>(emptySet())
        private set
    var corsiInclusi by mutableStateOf<List<Corso>>(emptyList())
        private set
    var messaggio by mutableStateOf("")
        private set
    var inCorso by mutableStateOf(false)
        private set

    fun scegliAbbonamento(abbonamento: Abbonamento) {
        if (inCorso) return
        abbonamentoScelto = abbonamento
        corsiSelezionati = emptySet()
        corsiInclusi = emptyList()
        messaggio = ""
        inCorso = true
        viewModelScope.launch {
            try {
                corsi = catalogo.corsi()
                corsiInclusi = catalogo.corsiInclusi(abbonamento.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messaggio = "Impossibile caricare i corsi inclusi."
            } finally {
                inCorso = false
            }
        }
    }

    fun scegliCorso(id: Long) {
        if (inCorso) return
        corsiSelezionati = if (id in corsiSelezionati) corsiSelezionati - id else corsiSelezionati + id
        messaggio = ""
    }

    fun associa() {
        if (inCorso) return
        val abbonamento = abbonamentoScelto
        val selezionati = corsiSelezionati.toList()
        if (abbonamento == null || selezionati.isEmpty()) {
            messaggio = "Seleziona almeno un corso."
            return
        }
        inCorso = true
        messaggio = ""
        viewModelScope.launch {
            try {
                catalogo.associaCorsi(abbonamento.id, selezionati)
                corsiInclusi = catalogo.corsiInclusi(abbonamento.id)
                corsiSelezionati = emptySet()
                messaggio = "Corsi associati correttamente."
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalStateException) {
                messaggio = e.message ?: "Associazione non riuscita."
            } catch (e: Exception) {
                messaggio = "Associazione non riuscita. Torna alla Home e riprova."
            } finally {
                inCorso = false
            }
        }
    }
}
