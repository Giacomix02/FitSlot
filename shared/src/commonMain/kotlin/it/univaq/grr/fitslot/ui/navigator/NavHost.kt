package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import it.univaq.grr.fitslot.ui.screens.associazioneCorsi.AssociazioneCorsiViewModel
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.CertificatiReceptionRepository
import it.univaq.grr.fitslot.domain.PermessiReception
import it.univaq.grr.fitslot.ui.screens.certificatiReception.*
import androidx.compose.runtime.Composable
import it.univaq.grr.fitslot.domain.PermessiGestione
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import it.univaq.grr.fitslot.ui.screens.CreazioneCorso.CorsiScreen
import it.univaq.grr.fitslot.ui.screens.HomeScreen
import it.univaq.grr.fitslot.ui.screens.associazioneSale.AssociazioneSaleViewModel
import it.univaq.grr.fitslot.ui.screens.certificatoMedico.CertificatoMedicoScreen
import it.univaq.grr.fitslot.ui.screens.LoginScreen.LoginScreen
import it.univaq.grr.fitslot.ui.screens.RegistrationScreen.RegistrationScreen
import it.univaq.grr.fitslot.ui.screens.UploadCertificatoMedico.UploadCertificatoMedicoScreen
import it.univaq.grr.fitslot.ui.screens.catalogo.CatalogoScreen
import it.univaq.grr.fitslot.ui.screens.catalogo.CatalogoViewModel
import it.univaq.grr.fitslot.ui.screens.catalogo.TipoCatalogo
import it.univaq.grr.fitslot.ui.screens.creazioneSala.CreazioneSalaScreen
import it.univaq.grr.fitslot.ui.screens.creazioneSala.CreazioneSalaViewModel
import kotlinx.serialization.Serializable

/**
 * @see [https://medium.com/@csabhionline/type-safe-navigation-in-kmp-cmp-950887dad65a]
 */
@Serializable
sealed class Screens {
    @Serializable
    data object Home

    @Serializable
    data object Login

    @Serializable
    data object CorsiScreen

    @Serializable
    data object CreazioneSala
    @Serializable
    data object Abbonamenti
    @Serializable
    data object PianiAPunti
    @Serializable
    data object Registration

    @Serializable
    data object UploadCertificatoMedicoScreen
  
    @Serializable
    data object CertificatoMedico

    @Serializable
    data object CertificatiReception

}

@Composable
fun Navigation(
    salaRepository: SalaRepository,
    catalogoRepository: CatalogoRepository,
    permessiGestione: PermessiGestione,
    content: @Composable () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.Home
    ) {
        composable<Screens.Home> {
            HomeScreen(navController, permessiGestione)
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
            if (PermessiReception.puoControllare()) {
                val vm = viewModel {
                    val db = AppDatabase.create()
                    CertificatiReceptionViewModel(CertificatiReceptionRepository(db.certificatiReceptionDao(), db.userDao()))
                }
                CertificatiReceptionScreen(vm, CertificatoMedicoRepositoryImpl(AppDatabase.create().certificatoMedicoDao())) { navController.popBackStack() }
            } else Column {
                Text("Accesso riservato alla reception.")
                TextButton(onClick = { navController.popBackStack() }) { Text("Torna alla Home") }
            }
        }

        composable<Screens.CorsiScreen> {
            CorsiScreen()
        }

        composable<Screens.UploadCertificatoMedicoScreen>{
            UploadCertificatoMedicoScreen(navController)
        }

        composable<Screens.CreazioneSala> {
            SoloGestore(permessiGestione, { navController.navigate(Screens.Login) }, { navController.popBackStack() }) {
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
            SoloGestore(permessiGestione, { navController.navigate(Screens.Login) }, { navController.popBackStack() }) {
                DestinazioneCatalogo(TipoCatalogo.ABBONAMENTI, catalogoRepository, salaRepository) { navController.popBackStack() }
            }
        }
        composable<Screens.PianiAPunti> {
            SoloGestore(permessiGestione, { navController.navigate(Screens.Login) }, { navController.popBackStack() }) {
                DestinazioneCatalogo(TipoCatalogo.PIANI_A_PUNTI, catalogoRepository) { navController.popBackStack() }
            }
        }
    }
    content()
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
private fun SoloGestore(permessi: PermessiGestione, onLogin: () -> Unit,
                        onIndietro: () -> Unit, content: @Composable () -> Unit) {
    if (permessi.puoGestire()) content()
    else Column {
        Text("Accesso riservato al gestore.")
        TextButton(onClick = onLogin) { Text("Accedi") }
        TextButton(onClick = onIndietro) { Text("Torna alla Home") }
    }
}
