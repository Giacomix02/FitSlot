package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import it.univaq.grr.fitslot.ui.screens.creazionesala.CreazioneSalaScreen
import it.univaq.grr.fitslot.ui.screens.creazionesala.CreazioneSalaViewModel

/** Collega la schermata di creazione della sala al suo ViewModel.
 * Il repository viene fornito dal punto di avvio dell'app. */

@Composable
fun App(salaRepository: SalaRepository) {
    val creazioneSalaViewModel = viewModel<CreazioneSalaViewModel> {
        CreazioneSalaViewModel(salaRepository)
    }

    // Le modifiche dello stato aggiornano automaticamente la schermata.
    val stato by creazioneSalaViewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CreazioneSalaScreen(
                stato = stato,
                onNomeModificato = creazioneSalaViewModel::aggiornaNome,
                onCapienzaModificata = creazioneSalaViewModel::aggiornaCapienza,
                onSalva = creazioneSalaViewModel::salva
            )
        }
    }
}