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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import fitslot.shared.generated.resources.Res
import fitslot.shared.generated.resources.compose_multiplatform
import it.univaq.grr.fitslot.Screens
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(navController: NavController) {
    var showContent by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Screen Home")
        Text("Verranno testati tutti gli screen e le relative navigazioni")
        Button(onClick = { navController.navigate(Screens.CreazioneSala) }) { Text("Crea sala") }
        Button(onClick = {navController.navigate(Screens.CorsiScreen)}) {
            Text("Pagina corsi")
        }
        Button(onClick = {navController.navigate(Screens.FasceOrarieScreen)}) {
            Text("Pagina Fasce orarie")
        }
        
        Button(onClick = { navController.navigate(Screens.Abbonamenti) }) { Text("Gestisci abbonamenti") }
        Button(onClick = { navController.navigate(Screens.PianiAPunti) }) { Text("Gestisci piani a punti") }
        Button(onClick = { navController.navigate(Screens.Login) }) {
            Text("Go to Login screen")
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