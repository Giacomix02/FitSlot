package it.univaq.grr.fitslot.ui.screens.UploadCertificatoMedico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
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
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.ui.screens.LoginScreen.LoginViewModel


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
    navController: NavController,
    viewModel: UploadCertificatoMedicoScreenViewModel = viewModel(factory = certificatoViewModelFactory),
) {
    var showDialog by remember { mutableStateOf(false) }

    // Create a DatePicker state
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Input
    )

    // Create a formatter
    val dateFormatter = remember {
        DatePickerDefaults.dateFormatter()
    }

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File("png", "jpg", "pdf"),
        onError = {  },
    ) { file ->
        // file is a PlatformFile, or null when the user cancelled
    }

    FloatingActionButton(
        onClick = { showDialog = true }
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Aggiungi certificato")
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
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
                        state = datePickerState,
                        dateFormatter = dateFormatter,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Button(
                        onClick = { launcher.launch() },
                    ){
                        Text("Inserisci Certificato")
                    }
                }

            }
        }
    }
}
