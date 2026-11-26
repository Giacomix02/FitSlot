package it.univaq.grr.fitslot.ui.screens.UploadCertificatoMedico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl


private val certificatoViewModelFactory = viewModelFactory {
    initializer {
        val database = AppDatabase.create()
        val certificatoDao = database.certificatoMedicoDao()
        val certificatoRepository = CertificatoMedicoRepositoryImpl(certificatoDao)
        UploadCertificatoMedicoScreenViewModel(certificatoRepository)
    }
}

@Composable
fun UploadCertificatoMedicoScreen(
    navController: NavController? = null,
    viewModel: UploadCertificatoMedicoScreenViewModel = viewModel(factory = certificatoViewModelFactory),
    utente: it.univaq.grr.fitslot.domain.models.User? = null,
    apriSubito: Boolean = false,
    onChiusura: () -> Unit = {},
    onSalvato: () -> Unit = {},
) {
    var showDialog by remember(utente?.id) { mutableStateOf(apriSubito) }
    LaunchedEffect(utente?.id) { viewModel.selezionaUtente(utente) }
    if (utente == null) {
        Text("Seleziona un iscritto dalla gestione certificati.")
        return
    }

    var certificatoSelezionato by remember { mutableStateOf(false) }

    // Create a DatePicker state
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Input
    )



    fun closeDialog() {
        showDialog = false
        certificatoSelezionato = false
        datePickerState.selectedDateMillis = null
        viewModel.resetSelections()
        onChiusura()
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        viewModel.onDateChange(datePickerState.selectedDateMillis)
    }

    // Create a formatter
    val dateFormatter = remember {
        DatePickerDefaults.dateFormatter()
    }

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File("png", "jpg", "pdf"),
        onError = { viewModel.error.value = true },
    ) { file ->
        certificatoSelezionato = file != null
        viewModel.onFileChange(file)
    }

    if (!apriSubito) FloatingActionButton(
        onClick = { showDialog = true }
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Aggiungi certificato")
    }

    if (showDialog) {
        Dialog(onDismissRequest = {
            if (!viewModel.inCorso) closeDialog()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DatePicker(
                        title = { Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = "Seleziona data di scadeza del certificato",
                                color = MaterialTheme.colorScheme.onPrimaryFixed
                            )
                        }},
                        state = datePickerState,
                        dateFormatter = dateFormatter,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Button(
                        onClick = { launcher.launch() },
                    ){
                        if(!certificatoSelezionato){
                            Text("Carica Certificato")
                        }else{
                            Text("Certificato Caricato")
                        }
                    }
                    if(viewModel.error.value){
                        Text("Si è verificato un errore")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(enabled = !viewModel.inCorso, onClick = {
                        closeDialog()
                    }) {
                        Text("Annulla")
                    }
                    TextButton(enabled = !viewModel.inCorso, onClick = { viewModel.uploadCertificato(
                        { showDialog = false; viewModel.resetSelections(); onSalvato() }
                    ) }) {
                        Text("Salva")
                    }
                }

            }
        }
    }
}
