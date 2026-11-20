package it.univaq.grr.fitslot.ui.screens.associazioneSale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AssociazioneSaleViewModel(
    private val catalogo: CatalogoRepository,
    private val saleRepository: SalaRepository
) : ViewModel() {
    var sale by mutableStateOf<List<Sala>>(emptyList())
        private set
    var abbonamentoScelto by mutableStateOf<Abbonamento?>(null)
        private set
    var salaScelta by mutableStateOf<Sala?>(null)
        private set
    var saleIncluse by mutableStateOf<List<Sala>>(emptyList())
        private set
    var messaggio by mutableStateOf("")
        private set
    var inCorso by mutableStateOf(false)
        private set

    fun scegliAbbonamento(abbonamento: Abbonamento) {
        if (inCorso) return
        abbonamentoScelto = abbonamento
        salaScelta = null
        saleIncluse = emptyList()
        messaggio = ""
        inCorso = true
        viewModelScope.launch {
            try {
                sale = saleRepository.osservaSale().first()
                saleIncluse = catalogo.saleIncluse(abbonamento.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messaggio = "Impossibile caricare le sale incluse."
            } finally {
                inCorso = false
            }
        }
    }

    fun scegliSala(sala: Sala) {
        if (inCorso) return
        salaScelta = sala
        messaggio = ""
    }

    fun associa() {
        if (inCorso) return
        val abbonamento = abbonamentoScelto
        val sala = salaScelta
        if (abbonamento == null || sala == null) {
            messaggio = "Seleziona un abbonamento e una sala."
            return
        }
        inCorso = true
        messaggio = ""
        viewModelScope.launch {
            try {
                catalogo.associaSala(abbonamento.id, sala.id)
                saleIncluse = catalogo.saleIncluse(abbonamento.id)
                salaScelta = null
                messaggio = "Sala associata correttamente."
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
