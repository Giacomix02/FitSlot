package it.univaq.grr.fitslot.ui.screens.certificatoMedico

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fitslot.shared.generated.resources.Res
import fitslot.shared.generated.resources.certificato_mock
import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import org.jetbrains.compose.resources.painterResource

@Composable
fun CertificatoMedicoScreen(
    navController: NavController,
    viewModel: CertificatoMedicoViewModel = viewModel { CertificatoMedicoViewModel() }
) {
    val utente = LocalStorage.getAll()["user"] as? User
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = { navController.popBackStack() }) { Text("Torna alla Home") }
        Text("Il mio certificato medico", style = MaterialTheme.typography.headlineMedium)
        if (utente?.ruolo != Ruolo.ISCRITTO) {
            Text("Accesso riservato all'iscritto.")
        } else if (!viewModel.certificatoPresente) {
            Text("Il tuo certificato medico non è presente nel sistema.")
        } else {
            Image(
                painter = painterResource(Res.drawable.certificato_mock),
                contentDescription = "Certificato medico di esempio, dati mock",
                modifier = Modifier.fillMaxWidth()
            )
            Text("Data di scadenza: " + viewModel.dataScadenza)
            if (viewModel.inScadenza()) {
                Text("Attenzione: il tuo certificato medico scade entro 20 giorni.")
            }
            Text("Documento e scadenza di esempio (mock).")
        }
    }
}
