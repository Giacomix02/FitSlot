package it.univaq.grr.fitslot.ui.screens.LogIngresso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import it.univaq.grr.fitslot.Screens
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.LogAccessoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.ui.components.DropDownSelector
import it.univaq.grr.fitslot.ui.components.VisualizzazioneCertificatoMedico
import it.univaq.grr.fitslot.utils.MediaStorage

private val logAccessoViewModelFactory = viewModelFactory {
    initializer {
        val database = AppDatabase.create()
        val accessoDao = database.logAccessoDao()
        val userDao = database.userDao()
        val certificatoMedicoDAO = database.certificatoMedicoDao()
        val logAccessoRepository = LogAccessoRepositoryImpl(accessoDao)
        val userRepository = UserRepositoryImpl(userDao)
        val certificatoMedicoRepository = CertificatoMedicoRepositoryImpl(certificatoMedicoDAO)
        LogAccessoViewModel(MediaStorage(), logAccessoRepository, userRepository, certificatoMedicoRepository)
    }
}

@Composable
fun LogAccessoScreen(
    navController: NavController,
    viewModel: LogAccessoViewModel = viewModel(factory = logAccessoViewModelFactory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)

    ) {
        Text("Le matricole iniziano da 1000")
        DropDownSelector(
            modifier = Modifier.fillMaxWidth(0.6f),
            searchable = true,
            isRequired = true,
            label = "Seleziona la matricola",
            items = viewModel.iscritti,
            itemLabel = { user ->
                user.matricola.toString()
            },
            customItemContent = { user ->
                Text(text = "${user.matricola} - ${user.nome} ${user.cognome}")
            },
            searchFilter = { user, query ->
                user.matricola.toString().contains(query.trim())
            },
            onSearchQueryChanged = {
                viewModel.onMatricolaChanged()
            },
            onItemSelected = { user ->
                viewModel.onUserSelected(user)
                viewModel.bringUpCertificato()
            },
            selectedItem = viewModel.selectedUser.value,
            errorMessage = if (viewModel.error) viewModel.textErrore else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions (onDone = { logIngresso(viewModel, navController) })  ,
        )

        if (uiState.showCertificato){
            VisualizzazioneCertificatoMedico(uiState.certificatoImage, uiState.certificatoScadenza, !uiState.isCertificatoValido)
        }
        Button(onClick = {
            logIngresso(viewModel, navController)
            viewModel.setShowCertificato(false)
        },
            enabled = uiState.isCertificatoValido) {
            Text("Registra Ingresso")
        }
    }
}

private fun logIngresso(
    viewModel: LogAccessoViewModel,
    navController: NavController
) {
    viewModel.onLog(
        onSuccess = {
            navController.navigate(Screens.Home)
        }
    )
}