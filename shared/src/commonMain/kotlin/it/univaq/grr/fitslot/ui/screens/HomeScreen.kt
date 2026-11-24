package it.univaq.grr.fitslot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import it.univaq.grr.fitslot.data.repository.PermessiGestioneDaLocalStorage
import it.univaq.grr.fitslot.domain.models.Ruolo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import it.univaq.grr.fitslot.domain.PermessiGestione
import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.domain.models.User
import fitslot.shared.generated.resources.Res
import fitslot.shared.generated.resources.compose_multiplatform
import it.univaq.grr.fitslot.Screens
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(navController: NavController, permessiGestione: PermessiGestione) {
    var showContent by remember { mutableStateOf(false) }
    val utente = LocalStorage.getAll()["user"] as? User
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Screen Home")
        if (utente?.ruolo == Ruolo.ISCRITTO) {
            Button(onClick = { navController.navigate(Screens.CertificatoMedico) }) {
                Text("Il mio certificato medico")
            }
        }
        Text("Verranno testati tutti gli screen e le relative navigazioni")
        if (permessiGestione.puoGestire()) Button(onClick = { navController.navigate(Screens.CreazioneSala) }) { Text("Gestisci sale") }
        Button(onClick = {navController.navigate(Screens.CorsiScreen)}) {
            Text("Pagina corsi")
        }
        Button(onClick = {navController.navigate(Screens.FasceOrarieScreen)}) {
            Text("Pagina Fasce orarie")
        }

        Button(onClick = { navController.navigate(Screens.Abbonamenti) }) { Text("Gestisci abbonamenti") }
        Button(onClick = { navController.navigate(Screens.PianiAPunti) }) { Text("Gestisci piani a punti") }
        if (permessiGestione.puoGestire()) Button(onClick = { navController.navigate(Screens.Abbonamenti) }) { Text("Gestisci abbonamenti") }
        if (permessiGestione.puoGestire()) Button(onClick = { navController.navigate(Screens.PianiAPunti) }) { Text("Gestisci piani a punti") }
        Button(onClick = { navController.navigate(Screens.Login) }) {
            Text("Go to Login screen")
        }
        if (PermessiGestioneDaLocalStorage.puoGestire(Ruolo.RECEPIONIST)) Button(onClick = { navController.navigate(Screens.CertificatiReception) }) {
            Text("Iscritti e certificati medici")

        Button(onClick = { navController.navigate(Screens.LogAccesso) }) {
            Text("Pagina registrazione accesso Iscritto")
        }
        Button(onClick = { navController.navigate(Screens.UploadCertificatoMedicoScreen) }) {
            Text("Carica Certificato Medico")
        }
    }
    AnimatedVisibility(showContent) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(painterResource(Res.drawable.compose_multiplatform), null)
            Text("Compose: siud")
        }
    }
}
