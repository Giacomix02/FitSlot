package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import it.univaq.grr.fitslot.Screens
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.ui.components.CustomTextField

private val loginViewModelFactory = viewModelFactory {
    initializer {
        val database = AppDatabase.create()
        val userDao = database.userDao()
        val userRepository = UserRepositoryImpl(userDao)
        LoginViewModel(userRepository = userRepository)
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(factory = loginViewModelFactory),
) {


    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CustomTextField(
            value = viewModel.emailInput,
            onValueChange = { viewModel.onEmailInputChange(it) },
            label = "Email",
            keyboardActions = KeyboardActions(onDone = { login(viewModel, navController) })
        )
        CustomTextField(
            value = viewModel.passwordInput,
            onValueChange = { viewModel.onPasswordInputChange(it) },
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(onDone = { login(viewModel, navController) })
        )
        if (viewModel.loginError) {
            Text("Email o password non validi")
        }
        Button(onClick = {
            login(viewModel, navController)
        }) {
           Text("Login")
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
        Button(
            onClick = {
                navController.navigate(Screens.Registration)
            }
        ) {
            Text("Oppure Registrati")
        }
    }
}

private fun login(
    viewModel: LoginViewModel,
    navController: NavController
) {
    viewModel.onLoginClick(
        onSuccess = {
            navController.navigate(Screens.Home)
        }
    )
}