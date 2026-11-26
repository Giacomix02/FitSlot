package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import it.univaq.grr.fitslot.ui.screens.associazioneCorsi.AssociazioneCorsiViewModel
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.ui.screens.certificatiReception.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import it.univaq.grr.fitslot.ui.navigator.AppDrawerContent
import it.univaq.grr.fitslot.ui.screens.CorsiScreen.CorsiScreen
import it.univaq.grr.fitslot.ui.screens.HomeScreen
import it.univaq.grr.fitslot.ui.screens.LogIngresso.LogAccessoScreen
import it.univaq.grr.fitslot.ui.screens.associazioneSale.AssociazioneSaleViewModel
import it.univaq.grr.fitslot.ui.screens.certificatoMedico.CertificatoMedicoScreen
import it.univaq.grr.fitslot.ui.screens.LoginScreen.LoginScreen
import it.univaq.grr.fitslot.ui.screens.RegistrationScreen.RegistrationScreen
import it.univaq.grr.fitslot.ui.screens.UploadCertificatoMedico.UploadCertificatoMedicoScreen
import it.univaq.grr.fitslot.ui.screens.catalogo.CatalogoScreen
import it.univaq.grr.fitslot.ui.screens.catalogo.CatalogoViewModel
import it.univaq.grr.fitslot.ui.screens.catalogo.TipoCatalogo
import it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen.FasceOrarieScreen
import it.univaq.grr.fitslot.ui.screens.creazioneSala.CreazioneSalaScreen
import it.univaq.grr.fitslot.ui.screens.creazioneSala.CreazioneSalaViewModel
import it.univaq.grr.fitslot.utils.GestioneUserLocalStorage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * @see [https://medium.com/@csabhionline/type-safe-navigation-in-kmp-cmp-950887dad65a]
 */
@Serializable
sealed class Screens {
    @Serializable
    data object Home : Screens()

    @Serializable
    data object Login : Screens()

    @Serializable
    data object CorsiScreen : Screens()

    @Serializable
    data object CreazioneSala : Screens()
    
    @Serializable
    data object FasceOrarieScreen : Screens()
    
    @Serializable
    data object Abbonamenti : Screens()
    @Serializable
    data object PianiAPunti : Screens()

    @Serializable
    data object LogAccesso: Screens()
    @Serializable
    data object Registration: Screens()

    @Serializable
    data object UploadCertificatoMedicoScreen: Screens()
  
    @Serializable
    data object CertificatoMedico: Screens()

    @Serializable
    data object CertificatiReception: Screens()

}

@Composable
fun Navigation(
    salaRepository: SalaRepository,
    catalogoRepository: CatalogoRepository,
    content: @Composable () -> Unit
) {
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // FAB hoistato: ogni schermata può registrare il proprio FAB tramite fab setter
    var fab by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    // Reset FAB ad ogni cambio di destinazione (evita race condition tra dispose/compose)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        fab = null
    }

    // si aggiorna ogni volta che cambnia user (quindi sia login che logout)
    val user = GestioneUserLocalStorage.getCurrentUser()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                user = user,
                navController = navController,
                onClose = { scope.launch { drawerState.close() } },
                onLogout = {
                    GestioneUserLocalStorage.logout()
                    // quando torno alla home cancello anche tutto lo stack della navigazione
                    navController.navigate(Screens.Home){ popUpTo(0) { inclusive = true } }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ){
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FitSlot") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                    // windowInsets = WindowInsets(0, 0, 0, 0) // decommentare se la topbar resta tagliata
                )
            },
            floatingActionButton = { fab?.invoke() },

        ){padding ->
            NavHost(
                navController = navController,
                startDestination = Screens.Home,
                modifier = Modifier.padding(padding)
            ) {
                composable<Screens.Home> {
                    HomeScreen(navController)
                }

                composable<Screens.Login> {
                    LoginScreen(navController)
                }

                composable<Screens.Registration> {
                    RegistrationScreen(navController)
                }

                composable<Screens.CertificatoMedico> {
                    CertificatoMedicoScreen(navController)
                }

                composable<Screens.CertificatiReception> {
                    if (GestioneUserLocalStorage.controllaUserRole(Ruolo.RECEPIONIST)) {
                        val vm = viewModel {
                            val db = AppDatabase.create()
                            CertificatiReceptionViewModel(CertificatoMedicoRepositoryImpl(db.certificatoMedicoDao()))
                        }
                        CertificatiReceptionScreen(vm, CertificatoMedicoRepositoryImpl(AppDatabase.create().certificatoMedicoDao())) { navController.popBackStack() }
                    } else Column {
                        Text("Accesso riservato alla reception.")
                        TextButton(onClick = { navController.popBackStack() }) { Text("Torna alla Home") }
                    }
                }

                composable<Screens.CorsiScreen> {
                    CorsiScreen(onFabChange = { fab = it })
                }

                composable<Screens.FasceOrarieScreen> {
                    FasceOrarieScreen(onFabChange = { fab = it })
                }

                composable<Screens.LogAccesso> {
                    LogAccessoScreen(navController)
                }
                composable<Screens.UploadCertificatoMedicoScreen>{
                    UploadCertificatoMedicoScreen(navController)
                }

                composable<Screens.CreazioneSala> {
                    SoloGestore( { navController.navigate(Screens.Login) }, { navController.popBackStack() }) {
                        val vm = viewModel { CreazioneSalaViewModel(salaRepository) }
                        val stato by vm.uiState.collectAsStateWithLifecycle()
                        Column(Modifier.fillMaxSize()) {
                            TextButton(onClick = { navController.popBackStack() }, enabled = !stato.salvataggioInCorso) { Text("Torna alla Home") }
                            CreazioneSalaScreen(stato, vm::aggiornaNome, vm::aggiornaCapienza, vm::salva,
                                vm::modifica, vm::annullaModifica, vm::richiediEliminazione,
                                vm::confermaEliminazione, vm::annullaEliminazione)
                        }
                    }
                }
                composable<Screens.Abbonamenti> {
                    SoloGestore( { navController.navigate(Screens.Login) }, { navController.popBackStack() }) {
                        DestinazioneCatalogo(TipoCatalogo.ABBONAMENTI, catalogoRepository, salaRepository) { navController.popBackStack() }
                    }
                }
                composable<Screens.PianiAPunti> {
                    SoloGestore( { navController.navigate(Screens.Login) }, { navController.popBackStack() }) {
                        DestinazioneCatalogo(TipoCatalogo.PIANI_A_PUNTI, catalogoRepository) { navController.popBackStack() }
                    }
                }
            }
            content()
        }
    }


}

@Composable
private fun DestinazioneCatalogo(tipo: TipoCatalogo, repository: CatalogoRepository, saleRepository: SalaRepository? = null, onIndietro: () -> Unit) {
    val vm = viewModel(key = tipo.name) { CatalogoViewModel(tipo, repository) }
    val stato by vm.uiState.collectAsStateWithLifecycle()
    val saleVm = if (saleRepository != null) viewModel { AssociazioneSaleViewModel(repository, saleRepository) } else null
    val corsiVm = if (saleRepository != null) viewModel { AssociazioneCorsiViewModel(repository) } else null
    CatalogoScreen(
        tipo, stato, vm::aggiornaNome, vm::aggiornaPrezzo, vm::aggiornaQuantita,
        vm::salva, vm::nuovo, vm::modifica, vm::richiediEliminazione,
        vm::confermaEliminazione, vm::annullaEliminazione, onIndietro, saleVm, corsiVm
    )
}

/** Legge soltanto i permessi della sessione già gestita dal team. */
@Composable
private fun SoloGestore( onLogin: () -> Unit,
                        onIndietro: () -> Unit, content: @Composable () -> Unit) {
    if (GestioneUserLocalStorage.controllaUserRole(Ruolo.GESTORE)) content()
    else Column {
        Text("Accesso riservato al gestore.")
        TextButton(onClick = onLogin) { Text("Accedi") }
        TextButton(onClick = onIndietro) { Text("Torna alla Home") }
    }
}
