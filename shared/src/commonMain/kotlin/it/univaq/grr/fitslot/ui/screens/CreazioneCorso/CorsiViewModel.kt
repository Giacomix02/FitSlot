package it.univaq.grr.fitslot.ui.screens.CreazioneCorso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.CorsoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.SalaRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.CorsoRepository
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
val fasceOrarie = listOf(
    FasciaOraria(1, LocalDateTime.parse("2027-06-01T15:30"),LocalDateTime.parse("2027-06-01T17:30") ),
    FasciaOraria(2, LocalDateTime.parse("2027-06-01T18:30"),LocalDateTime.parse("2027-06-01T19:30") ),
    FasciaOraria(3, LocalDateTime.parse("2027-06-02T12:30"),LocalDateTime.parse("2027-06-02T15:00") )
)

data class CorsiScreenState(
    var showDialog: Boolean = false,
    var showSaleDropDown: Boolean = false,
    var showFasceOrarieDropDown: Boolean = false,
    var erroreCreazioneCorso: String? = null,
    var showCreazioneCorsoEseguita: Boolean = false
)

class CorsiViewModel(
    val corsoRepository: CorsoRepository = CorsoRepositoryImpl(AppDatabase.create().corsoDao()),
    val salaRepository: SalaRepository = SalaRepositoryImpl(AppDatabase.create().salaDao())
) : ViewModel() {
    val uiState: StateFlow<CorsiScreenState> field = MutableStateFlow(CorsiScreenState())

    val saleList = salaRepository.osservaSale()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    val fasceOrarieList: List<FasciaOraria> field = MutableList(fasceOrarie.size) { index -> fasceOrarie[index] }

    init {
        // Le fasce orarie devono esistere nel database per poter confrontare
        // gli orari dei corsi tramite la query di sovrapposizione.
        viewModelScope.launch {
            runCatching { corsoRepository.salvaFasceOrarie(fasceOrarie) }
        }
    }

    fun showDialog() { uiState.update { it.copy(showDialog = true) } }

    fun onFieldInteraction() { uiState.update { it.copy(showCreazioneCorsoEseguita = false, erroreCreazioneCorso = null) } }

    fun hideDialog() { uiState.update { it.copy(showDialog = false, showCreazioneCorsoEseguita = false, erroreCreazioneCorso = null) } }

    fun setSaleDropdown(show: Boolean) {
        uiState.update { it.copy(showSaleDropDown = show) }
    }

    fun setFasceOrarieDropdown(show: Boolean) {
        uiState.update { it.copy(showFasceOrarieDropDown = show) }
    }

    /** Sale che non hanno corsi in una fascia sovrapposta a [fasciaOraria]. */
    suspend fun saleLibereInFascia(fasciaOraria: FasciaOraria): List<Sala> {
        val saleOccupate = corsoRepository.saleOccupate(fasciaOraria)
        return saleList.value.filterNot { it.id in saleOccupate }
    }

    /** Fasce orarie che non si sovrappongono a corsi già assegnati alla [sala]. */
    suspend fun fasceLibereInSala(sala: Sala): List<FasciaOraria> {
        val fasceOccupate = corsoRepository.fasceOccupate(sala.id)
        return fasceOrarieList.filter { fascia ->
            fasceOccupate.none { occupata -> occupata.siSovrapponeCon(fascia) }
        }
    }

    suspend fun creaCorso(nome: String, sala: Sala, fasciaOraria: FasciaOraria): Boolean {
        // Controllo nel database: la sala deve essere libera nella fascia scelta.
        val salaOccupata = try {
            corsoRepository.salaOccupata(sala.id, fasciaOraria)
        } catch (e: Exception) {
            uiState.update { it.copy(erroreCreazioneCorso = e.message ?: e.toString()) }
            return false
        }
        if (salaOccupata) {
            uiState.update {
                it.copy(
                    erroreCreazioneCorso = "La sala ${sala.nome} è già occupata in questa fascia oraria.",
                    showCreazioneCorsoEseguita = false
                )
            }
            return false
        }

        val corso: Corso
        try {
            corso = Corso(name= nome, salaAssegnata = sala, fasciaOraria = fasciaOraria)
        } catch (e: IllegalArgumentException) {
            uiState.update { it.copy(erroreCreazioneCorso = e.message) }
            return false
        }

        try {
            corsoRepository.creaCorso(corso)
            uiState.update { it.copy(erroreCreazioneCorso = null, showCreazioneCorsoEseguita = true) }
            return true
        } catch (e: Exception) {
            uiState.update { it.copy(erroreCreazioneCorso = e.message ?: e.toString()) }
            return false
        }
    }
}