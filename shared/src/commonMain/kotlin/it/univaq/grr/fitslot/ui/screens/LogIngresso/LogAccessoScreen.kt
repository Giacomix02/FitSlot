package it.univaq.grr.fitslot.ui.screens.LogIngresso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import it.univaq.grr.fitslot.Screens
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.LogAccessoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.ui.components.DropDownSelector

private val logAccessoViewModelFactory = viewModelFactory {
    initializer {
        val database = AppDatabase.create()
        val accessoDao = database.logAccessoDao()
        val userDao = database.userDao()
        val logAccessoRepository = LogAccessoRepositoryImpl(accessoDao)
        val userRepository = UserRepositoryImpl(userDao)
        LogAccessoViewModel(logAccessoRepository, userRepository)
    }
}

@Composable
fun LogAccessoScreen(
    navController: NavController,
    viewModel: LogAccessoViewModel = viewModel(factory = logAccessoViewModelFactory),
) {
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
                (user.id + 999).toString()
            },
            customItemContent = { user ->
                Text(text = "${user.id + 999} - ${user.nome} ${user.cognome}")
            },
            searchFilter = { user, query ->
                (user.id + 999).toString().contains(query.trim())
            },
            onSearchQueryChanged = {
                viewModel.onMatricolaChanged()
            },
            onItemSelected = { user ->
                viewModel.onUserSelected(user)
            },
            selectedItem = viewModel.selectedUser.value,
            errorMessage = if (viewModel.error) viewModel.textErrore else null,
        )
        if (viewModel.error) {
            Text(viewModel.textErrore)
        }
        Button(onClick = {
            viewModel.onLog (
                onSuccess = {
                    navController.navigate(Screens.Home)
                }
            )
        }) {
            Text("Registra Ingresso")
        }
    }
}