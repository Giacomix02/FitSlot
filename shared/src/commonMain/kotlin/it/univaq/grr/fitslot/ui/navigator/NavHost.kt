package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.repository.*
import it.univaq.grr.fitslot.ui.screens.HomeScreen
import it.univaq.grr.fitslot.ui.screens.TestScreen
import it.univaq.grr.fitslot.ui.screens.LoginScreen.*
import it.univaq.grr.fitslot.ui.screens.creazionesala.*
import it.univaq.grr.fitslot.ui.screens.catalogo.*
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens {
    @Serializable data object Home
    @Serializable data class Test(val message: String)
    @Serializable data object Login
    @Serializable data object CreazioneSala
    @Serializable data object Abbonamenti
    @Serializable data object PianiAPunti
}

/** La sessione e i repository rimangono gli stessi tra tutte le schermate. */
@Composable
fun Navigation(salaRepository: SalaRepository, userRepository: UserRepository,
               catalogoRepository: CatalogoRepository, sessione: SessioneUtente) {
    var configurato by remember { mutableStateOf<Boolean?>(null) }
    var erroreAvvio by remember { mutableStateOf(false) }
    var tentativoAvvio by remember { mutableIntStateOf(0) }
    LaunchedEffect(tentativoAvvio) {
        erroreAvvio = false
        try { configurato = !userRepository.serveConfigurazione() }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { erroreAvvio = true }
    }
    if (erroreAvvio) {
        Column {
            Text("Impossibile aprire il database.")
            TextButton(onClick = { tentativoAvvio++ }) { Text("Riprova") }
        }
        return
    }
    if (configurato == null) { Text("Caricamento..."); return }
    if (configurato == false) {
        val vm = viewModel { ConfigurazioneGestoreViewModel(userRepository, sessione) }
        ConfigurazioneGestoreScreen(vm) { configurato = true }
        return
    }
    val navController = rememberNavController()
    val utente by sessione.utente.collectAsStateWithLifecycle()
    val gestore = utente?.ruolo == Ruolo.GESTORE
    NavHost(navController = navController, startDestination = Screens.Home) {
        composable<Screens.Home> { HomeScreen(navController, sessione) }
        composable<Screens.Test> { TestScreen(navController, it.toRoute<Screens.Test>()) }
        composable<Screens.Login> {
            val vm = viewModel { LoginViewModel(userRepository, sessione) }
            LoginScreen(vm) {
                navController.navigate(Screens.Home) { popUpTo<Screens.Home> { inclusive = true } }
            }
        }
        composable<Screens.CreazioneSala> {
            if (gestore) {
                val vm = viewModel { CreazioneSalaViewModel(salaRepository) }
                val stato by vm.uiState.collectAsStateWithLifecycle()
                Column(Modifier.fillMaxSize()) {
                    TextButton(onClick = { navController.popBackStack() }, enabled = !stato.salvataggioInCorso) { Text("Torna alla Home") }
                    CreazioneSalaScreen(stato, vm::aggiornaNome, vm::aggiornaCapienza, vm::salva)
                }
            } else AccessoRiservato { navController.popBackStack() }
        }
        composable<Screens.Abbonamenti> {
            if (gestore) DestinazioneCatalogo(TipoCatalogo.ABBONAMENTI, catalogoRepository) { navController.popBackStack() }
            else AccessoRiservato { navController.popBackStack() }
        }
        composable<Screens.PianiAPunti> {
            if (gestore) DestinazioneCatalogo(TipoCatalogo.PIANI_A_PUNTI, catalogoRepository) { navController.popBackStack() }
            else AccessoRiservato { navController.popBackStack() }
        }
    }
}

@Composable
private fun DestinazioneCatalogo(tipo: TipoCatalogo, repository: CatalogoRepository, onIndietro: () -> Unit) {
    val vm = viewModel(key = tipo.name) { CatalogoViewModel(tipo, repository) }
    val stato by vm.uiState.collectAsStateWithLifecycle()
    CatalogoScreen(tipo, stato, vm::aggiornaNome, vm::aggiornaPrezzo, vm::aggiornaQuantita,
        vm::salva, vm::nuovo, vm::modifica, vm::richiediEliminazione,
        vm::confermaEliminazione, vm::annullaEliminazione, onIndietro)
}

@Composable
private fun AccessoRiservato(onIndietro: () -> Unit) {
    Column {
        Text("Accesso riservato al gestore autenticato.")
        TextButton(onClick = onIndietro) { Text("Torna indietro") }
    }
}
