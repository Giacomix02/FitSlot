package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.univaq.grr.fitslot.Screens
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl

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
        OutlinedTextField(
            value = viewModel.emailInput,
            onValueChange = { viewModel.onEmailInputChange(it) },
            label = {Text("Email") }
        )
        OutlinedTextField(
            value = viewModel.passwordInput,
            onValueChange = { viewModel.onPasswordInputChange(it) },
            label = {Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Button(onClick = {
            viewModel.onLoginClick (
                onSuccess = {
                    navController.navigate(Screens.Home)
                }
            )
        }) {
           Text("Login")
        }
        if (viewModel.loginError) {
            Text("Email o password non validi")
        }
    }
}