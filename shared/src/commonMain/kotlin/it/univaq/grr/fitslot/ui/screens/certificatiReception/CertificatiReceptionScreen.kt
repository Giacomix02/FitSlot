package it.univaq.grr.fitslot.ui.screens.certificatiReception

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.univaq.grr.fitslot.utils.dataScadenza
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.ui.screens.UploadCertificatoMedico.*

@Composable
fun CertificatiReceptionScreen(vm: CertificatiReceptionViewModel, repositoryCertificati: CertificatoMedicoRepository, onIndietro: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            TextButton(onClick = onIndietro, enabled = !vm.inCorso) { Text("Torna alla Home") }
            Text("Iscritti e certificati medici", style = MaterialTheme.typography.headlineMedium)
            Row {
                Button(onClick = { vm.cerca(null) }, enabled = !vm.inCorso) { Text("Tutti gli iscritti") }
                TextButton(onClick = { vm.cerca(true) }, enabled = !vm.inCorso) { Text("Mostra scaduti") }
            }
            OutlinedTextField(vm.dal, { vm.dal = it }, label = { Text("Dal (AAAA-MM-GG)") }, singleLine = true, enabled = !vm.inCorso)
            OutlinedTextField(vm.al, { vm.al = it }, label = { Text("Al (AAAA-MM-GG)") }, singleLine = true, enabled = !vm.inCorso)
            Button(onClick = { vm.cerca(false) }, enabled = !vm.inCorso) { Text("Cerca in scadenza") }
            if (vm.inCorso) Text("Caricamento...")
            Text(vm.messaggio)
        }
        items(vm.elenco, key = { it.id }) { iscritto ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${iscritto.nome} ${iscritto.cognome}")
                    Text(iscritto.email)
                    Text("Scadenza: ${dataScadenza(iscritto.scadenza) ?: "certificato assente o data non valida"}")
                    TextButton(onClick = { vm.scegliIscritto(iscritto.id) }, enabled = !vm.inCorso) { Text("Carica certificato") }
                }
            }
        }
    }
    val utente = vm.utenteScelto
    if (utente != null) {
        val uploadVm = viewModel(key = "certificato-${utente.id}") {
            UploadCertificatoMedicoScreenViewModel(repositoryCertificati)
        }
        UploadCertificatoMedicoScreen(viewModel = uploadVm, utente = utente, apriSubito = true,
            onChiusura = vm::chiudiCaricamento, onSalvato = vm::certificatoSalvato)
    }
}
