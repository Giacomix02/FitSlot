package it.univaq.grr.fitslot.ui.screens.CorsiScreen

import androidx.lifecycle.ViewModel
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.CorsoRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.repository.CorsoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

val sale = listOf(
    Sala(1, "fitness", 10),
    Sala(2, "pesi", 18),
    Sala(3, "yoga", 20)
)

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

class CorsiViewModel: ViewModel() {
    val database = AppDatabase.create()
    val corsoRepository: CorsoRepository = CorsoRepositoryImpl(database.corsoDao())
    val uiState: StateFlow<CorsiScreenState> field = MutableStateFlow(CorsiScreenState())

    val saleList: List<Sala> field = MutableList<Sala>(sale.size) { index -> sale[index] }
    val fasceOrarieList: List<FasciaOraria> field = MutableList(fasceOrarie.size) { index -> fasceOrarie[index] }

    fun showDialog() { uiState.update { it.copy(showDialog = true) } }

    fun onFieldInteraction() { uiState.update { it.copy(showCreazioneCorsoEseguita = false, erroreCreazioneCorso = null) } }

    fun hideDialog() { uiState.update { it.copy(showDialog = false, showCreazioneCorsoEseguita = false, erroreCreazioneCorso = null) } }

    fun setSaleDropdown(show: Boolean) {
        uiState.update { it.copy(showSaleDropDown = show) }
    }

    fun setFasceOrarieDropdown(show: Boolean) {
        uiState.update { it.copy(showFasceOrarieDropDown = show) }
    }

    suspend fun creaCorso(nome: String, sala: Sala, fasciaOraria: FasciaOraria) {
        val corso: Corso
        try {
            corso = Corso(name= nome, salaAssegnata = sala, fasciaOraria = fasciaOraria)
        } catch (e: IllegalArgumentException) {
            uiState.update { it.copy(erroreCreazioneCorso = e.message) }
            return
        }
//        uiState.update { it.copy(erroreCreazioneCorso = null, showCreazioneCorsoEseguita = true) }

        try {
            corsoRepository.creaCorso(corso)
            uiState.update { it.copy(showCreazioneCorsoEseguita = true) }
        } catch (e: Exception) {
            uiState.update { it.copy(erroreCreazioneCorso = e.message ?: e.toString()) }
        }




    }
}