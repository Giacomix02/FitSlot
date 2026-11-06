package it.univaq.grr.fitslot.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import it.univaq.grr.fitslot.Screens

@Composable
fun TestScreen(navController: NavController, passedObj: Screens.Test){
    Text(passedObj.message)
    
}