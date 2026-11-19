package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.FasciaOrariaRepositoryImpl
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.repository.FasciaOrariaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


class FasceOrarieViewModel : ViewModel(){
    val database = AppDatabase.create()

    val fasciaOrariaRepository: FasciaOrariaRepository = FasciaOrariaRepositoryImpl(database.fasciaOrariaDao())

    val selectedFasciaOraria = mutableStateOf(FasciaOraria())

    val fasceOrarie = fasciaOrariaRepository.observeFasceOrarie().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun setDefaultFasciaOrariaState(orarioInizioState: TimePickerState, orarioFineState: TimePickerState)
        = setSelectedFasciaOraria(orarioInizioState, orarioFineState, FasciaOraria())


    fun setSelectedFasciaOraria(orarioInizioState: TimePickerState, orarioFineState: TimePickerState, fasciaOraria: FasciaOraria){
        selectedFasciaOraria.value = fasciaOraria
        orarioInizioState.hour = fasciaOraria.orarioInizio.hour
        orarioInizioState.minute = fasciaOraria.orarioInizio.minute
        orarioFineState.hour = fasciaOraria.orarioFine.hour
        orarioFineState.minute = fasciaOraria.orarioFine.minute
    }

    suspend fun creaFasciaOraria(
        orarioInizio: TimePickerState,
        orarioFine: TimePickerState,
        selectedDate: MutableState<String>
    ) {
        val date = LocalDate.parse(selectedDate.value, formatter)

        val start = LocalTime.of(orarioInizio.hour, orarioInizio.minute)
        val end = LocalTime.of(orarioFine.hour, orarioFine.minute)
        val fasciaOraria = FasciaOraria(
            orarioInizio = LocalDateTime.of(date, start),
            orarioFine = LocalDateTime.of(date, end)
        )
        fasciaOrariaRepository.createFasciaOraria(fasciaOraria)

    }

    suspend fun modificaFasciaOraria(orarioInizio: TimePickerState, orarioFine: TimePickerState, selectedDate: MutableState<String>) {
        val date = LocalDate.parse(selectedDate.value, formatter)

        val start = LocalTime.of(orarioInizio.hour, orarioInizio.minute)
        val end = LocalTime.of(orarioFine.hour, orarioFine.minute)

        val fasciaOraria = FasciaOraria(id = selectedFasciaOraria.value.id)

        fasciaOraria.orarioInizio = LocalDateTime.of(date, start)
        fasciaOraria.orarioFine = LocalDateTime.of(date, end)

        fasciaOrariaRepository.editFasciaOraria(fasciaOraria)

        selectedFasciaOraria.value
    }

    suspend fun deleteFasciaOraria(fasciaOraria: FasciaOraria) {
        fasciaOrariaRepository.deleteFasciaOraria(fasciaOraria)
    }
//    fun showEditFasciaOraria(fasciaOraria: FasciaOraria): () -> Unit {
//
//    }


}