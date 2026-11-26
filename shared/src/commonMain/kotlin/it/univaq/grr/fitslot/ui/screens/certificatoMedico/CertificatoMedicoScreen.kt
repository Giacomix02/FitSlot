package it.univaq.grr.fitslot.ui.screens.certificatoMedico

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.utils.immagineCertificato
import it.univaq.grr.fitslot.utils.dataOggi
import it.univaq.grr.fitslot.ui.components.VisualizzazioneCertificatoMedico

@Composable
fun CertificatoMedicoScreen(navController: NavController,
    viewModel: CertificatoMedicoViewModel = viewModel {
        CertificatoMedicoViewModel(CertificatoMedicoRepositoryImpl(AppDatabase.create().certificatoMedicoDao()))
    }) {
    val utente = LocalStorage.getAll()["user"] as? User
    LaunchedEffect(utente?.id) {
        if (utente?.ruolo == Ruolo.ISCRITTO) viewModel.carica(utente)
    }
    val immagine = remember(viewModel.documento) {
        val bytes = viewModel.documento
        if (bytes == null || viewModel.percorso.endsWith(".pdf", true)) null
        else immagineCertificato(bytes)
    }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = { navController.popBackStack() }) { Text("Torna alla Home") }
        Text("Il mio certificato medico", style = MaterialTheme.typography.headlineMedium)
        if (utente?.ruolo != Ruolo.ISCRITTO) Text("Accesso riservato all'iscritto.")
        else if (viewModel.inCorso) Text("Caricamento...")
        else {
            Text(viewModel.messaggio)
            if (!viewModel.presente && viewModel.messaggio.isEmpty()) Text("Il tuo certificato medico non è presente nel sistema.")
            if (viewModel.presente) {
                Text("Data di scadenza: ${viewModel.scadenza ?: "non valida, contatta la reception"}")
                if (certificatoInScadenza(viewModel.scadenza, dataOggi())) Text("Attenzione: il tuo certificato medico scade entro 20 giorni.")
                val scadenza = viewModel.scadenza

                VisualizzazioneCertificatoMedico(null, scadenza , certificatoInScadenza(viewModel.scadenza, dataOggi()))

            }
            TextButton(onClick = { viewModel.carica(utente) }) { Text("Ricarica") }
        }
    }
}
