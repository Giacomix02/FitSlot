package it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen

import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


class FasceOrarieViewModel : ViewModel(){

    val selectedFasciaOraria = mutableStateOf<FasciaOraria>(FasciaOraria())

    fun setDefaultFasciaOrariaState(orarioInizioState: TimePickerState, orarioFineState: TimePickerState)
        = setSelectedFasciaOraria(orarioInizioState, orarioFineState, FasciaOraria())


    fun setSelectedFasciaOraria(orarioInizioState: TimePickerState, orarioFineState: TimePickerState, fasciaOraria: FasciaOraria){
        selectedFasciaOraria.value = fasciaOraria
        orarioInizioState.hour = fasciaOraria.orarioInizio.hour
        orarioInizioState.minute = fasciaOraria.orarioInizio.minute
        orarioFineState.hour = fasciaOraria.orarioFine.hour
        orarioFineState.minute = fasciaOraria.orarioFine.minute
    }

    fun creaFasciaOraria(
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
//        fasciaOrariaRepository.createFasciaOraria(fasciaOraria)

    }

    fun modificaFasciaOraria() {
        TODO("Not yet implemented")
    }

    fun deleteFasciaOraria(fasciaOraria: FasciaOraria) {}
//    fun showEditFasciaOraria(fasciaOraria: FasciaOraria): () -> Unit {
//
//    }

}