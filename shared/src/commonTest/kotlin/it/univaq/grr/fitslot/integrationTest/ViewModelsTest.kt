package it.univaq.grr.fitslot.integrationTest

import it.univaq.grr.fitslot.Vals
import it.univaq.grr.fitslot.ui.screens.CorsiScreen.CorsiViewModel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewModelsTest {


    @Test
    fun creazioneCorsoVisibilitaFeedbackTest() {
        val viewModel = CorsiViewModel()
        val uiState = viewModel.uiState

        runBlocking { viewModel.creaCorso("    ",
            sala = Vals.sala,
            fasciaOraria = Vals.fasceOrarie[0]
        ) }

        assertNotNull(
            uiState.value.erroreCreazioneCorso,
            "Errore di creazione corso non visibile"
        )
        viewModel.onFieldInteraction()
        assertNull(
            uiState.value.erroreCreazioneCorso,
            "Errore creazione corso non scompare quando succede interazione"
        )
        assertFalse(
            uiState.value.showCreazioneCorsoEseguita,
            "icona creazione corso non scompare quando succede interazione"
        )

        runBlocking{ viewModel.creaCorso("T",
            sala = Vals.sala,
            fasciaOraria = Vals.fasceOrarie[0]
        ) }

        assertTrue(
            uiState.value.showCreazioneCorsoEseguita,
            "Icona creazione corso non appare quando viene creato il corso"
        )
    }
}