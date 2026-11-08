package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation

/** Riceve il ViewModel senza aprire un secondo database. */
@Composable
fun LoginScreen(viewModel: LoginViewModel, onSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(viewModel.emailInput, viewModel::onEmailInputChange,
            label = { Text("Email") }, singleLine = true, enabled = !viewModel.loginInCorso)
        OutlinedTextField(viewModel.passwordInput, viewModel::onPasswordInputChange,
            label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), enabled = !viewModel.loginInCorso)
        Button(onClick = { viewModel.onLoginClick(onSuccess) }, enabled = !viewModel.loginInCorso) {
            Text(if (viewModel.loginInCorso) "Accesso in corso..." else "Login")
        }
        if (viewModel.loginError) Text("Email o password non validi")
        viewModel.erroreSistema?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
