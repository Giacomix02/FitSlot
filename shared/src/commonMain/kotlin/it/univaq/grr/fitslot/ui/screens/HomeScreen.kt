package it.univaq.grr.fitslot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import it.univaq.grr.fitslot.Screens
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.models.Ruolo

/** Mostra le operazioni disponibili in base al ruolo dell'utente autenticato. */
@Composable
fun HomeScreen(navController: NavController, sessione: SessioneUtente) {
    val utente by sessione.utente.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("FitSlot", style = MaterialTheme.typography.headlineLarge)
        if (utente == null) {
            Text("Accedi per visualizzare la tua dashboard.")
            Button(onClick = { navController.navigate(Screens.Login) }) { Text("Login") }
        } else {
            Text("Benvenuto, ${utente!!.nome}", style = MaterialTheme.typography.titleLarge)
            Text("Ruolo: ${utente!!.ruolo}")
            if (utente!!.ruolo == Ruolo.GESTORE) {
                Button(onClick = { navController.navigate(Screens.CreazioneSala) }) { Text("Crea sala") }
                Button(onClick = { navController.navigate(Screens.Abbonamenti) }) { Text("Gestisci abbonamenti") }
                Button(onClick = { navController.navigate(Screens.PianiAPunti) }) { Text("Gestisci piani a punti") }
            } else {
                Text("Le funzioni di gestione sono riservate al gestore.")
            }
            TextButton(onClick = {
                sessione.esci()
                navController.navigate(Screens.Home) { popUpTo<Screens.Home> { inclusive = true } }
            }) { Text("Esci") }
        }
        TextButton(onClick = { navController.navigate(Screens.Test("test message")) }) {
            Text("Go to test screen")
        }
    }
}
