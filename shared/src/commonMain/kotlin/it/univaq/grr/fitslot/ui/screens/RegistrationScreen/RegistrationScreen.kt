package it.univaq.grr.fitslot.ui.screens.LoginScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import it.univaq.grr.fitslot.Screens
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.ui.components.CustomTextField

private val registrationViewModelFactory = viewModelFactory {
    initializer {
        val database = AppDatabase.create()
        val userDao = database.userDao()
        val userRepository = UserRepositoryImpl(userDao)
        RegistrationViewModel(userRepository = userRepository)
    }
}

@Composable
fun RegistrationScreen(
    navController: NavController,
    viewModel: RegistrationViewModel = viewModel(factory = registrationViewModelFactory),
) {


    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Input
    )

    val dateFormatter = remember { DatePickerDefaults.dateFormatter() }
    LaunchedEffect(datePickerState.selectedDateMillis) {
        viewModel.onDateChange(datePickerState.selectedDateMillis)
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        viewModel.fields.forEach { field ->
            CustomTextField(
                value = viewModel.valueOf(field.key),
                onValueChange = { value ->
                    viewModel.onValueChange(field.key, value)
                },
                label = field.label,
                isRequired = field.required,
                errorMessage = viewModel.errorOf(field),
                visualTransformation = if (field.key == "password") {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .border(
                    width = 1.dp,
                    color = if (viewModel.dateErrorMessage() != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(1.dp)
        ) {
            DatePicker(
                state = datePickerState,
                dateFormatter = dateFormatter,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = 1f,
                        scaleY = 1f,
                        transformOrigin = TransformOrigin(0f, 0f)
                    )
            )
        }


        if (viewModel.registrationError.value) {
            Text(
                text = viewModel.registrationErrorText.value,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                ),
                onClick = {
                navController.navigate(Screens.Login)
            }) {

                Text("Annulla")
            }
            Button(onClick = {
                viewModel.onRegisterClick (
                    onSuccess = {
                        navController.navigate(Screens.Home)
                    }
                )
            }) {
                Text("Registrati")
            }
        }
    }
}