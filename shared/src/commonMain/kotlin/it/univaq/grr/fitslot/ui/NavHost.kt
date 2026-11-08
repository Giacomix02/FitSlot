package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import it.univaq.grr.fitslot.ui.screens.HomeScreen
import it.univaq.grr.fitslot.ui.screens.TestScreen
import it.univaq.grr.fitslot.ui.screens.creazionesala.CreazioneSalaScreen
import it.univaq.grr.fitslot.ui.screens.creazionesala.CreazioneSalaViewModel
import kotlinx.serialization.Serializable

/**
 * Destinazioni disponibili nella navigazione.
 */
@Serializable
sealed class Screens {
    @Serializable
    data object Home

    @Serializable
    data class Test(val message: String)

    @Serializable
    data object CreazioneSala
}

/**
 * Collega ogni destinazione alla schermata corrispondente.
 */
@Composable
fun Navigation(salaRepository: SalaRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.Home
    ) {
        composable<Screens.Home> {
            HomeScreen(navController)
        }

        composable<Screens.Test> { backStackEntry ->
            val args = backStackEntry.toRoute<Screens.Test>()
            TestScreen(navController, args)
        }

        composable<Screens.CreazioneSala> {
            // Il ViewModel è associato a questa destinazione.
            val creazioneSalaViewModel = viewModel<CreazioneSalaViewModel> {
                CreazioneSalaViewModel(salaRepository)
            }

            val stato by creazioneSalaViewModel.uiState
                .collectAsStateWithLifecycle()

            Column(modifier = Modifier.fillMaxSize()) {
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Torna alla Home")
                }

                CreazioneSalaScreen(
                    stato = stato,
                    onNomeModificato = creazioneSalaViewModel::aggiornaNome,
                    onCapienzaModificata =
                        creazioneSalaViewModel::aggiornaCapienza,
                    onSalva = creazioneSalaViewModel::salva
                )
            }
        }
    }
}