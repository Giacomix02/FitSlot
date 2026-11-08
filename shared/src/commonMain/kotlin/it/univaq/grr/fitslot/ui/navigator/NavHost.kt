package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import it.univaq.grr.fitslot.domain.repository.UserRepository
import it.univaq.grr.fitslot.ui.screens.HomeScreen
import it.univaq.grr.fitslot.ui.screens.TestScreen
import it.univaq.grr.fitslot.ui.screens.LoginScreen.LoginScreen
import it.univaq.grr.fitslot.ui.screens.LoginScreen.LoginViewModel
import it.univaq.grr.fitslot.ui.screens.creazionesala.*
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens {
    @Serializable data object Home
    @Serializable data class Test(val message: String)
    @Serializable data object Login
    @Serializable data object CreazioneSala
}

/** Il login usa lo stesso database delle altre schermate. */
@Composable
fun Navigation(salaRepository: SalaRepository, userRepository: UserRepository) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Home) {
        composable<Screens.Home> { HomeScreen(navController) }
        composable<Screens.Test> {
            TestScreen(navController, it.toRoute<Screens.Test>())
        }
        composable<Screens.Login> {
            val loginViewModel = viewModel { LoginViewModel(userRepository) }
            LoginScreen(loginViewModel) {
                navController.navigate(Screens.Home) {
                    popUpTo<Screens.Home> { inclusive = true }
                }
            }
        }
        composable<Screens.CreazioneSala> {
            val vm = viewModel { CreazioneSalaViewModel(salaRepository) }
            val stato by vm.uiState.collectAsStateWithLifecycle()
            Column(Modifier.fillMaxSize()) {
                TextButton(onClick = { navController.popBackStack() }) { Text("Torna alla Home") }
                CreazioneSalaScreen(stato, vm::aggiornaNome, vm::aggiornaCapienza, vm::salva)
            }
        }
    }
}
