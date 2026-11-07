package it.univaq.grr.fitslot

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.univaq.grr.fitslot.ui.screens.HomeScreen
import it.univaq.grr.fitslot.ui.screens.LoginScreen
import it.univaq.grr.fitslot.ui.screens.TestScreen
import kotlinx.serialization.Serializable
/**
 * @see [https://medium.com/@csabhionline/type-safe-navigation-in-kmp-cmp-950887dad65a]
 */
@Serializable
sealed class Screens {
    @Serializable
    data object Home 
    
    @Serializable
    data class Test (val message:String)
    @Serializable
    data object Login
}

@Composable
fun Navigation(content: @Composable () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.Home
    ) {
        composable<Screens.Home> {
            HomeScreen(navController)
        }
        composable<Screens.Test> { backStackEntry ->
            val args: Screens.Test = backStackEntry.toRoute() // argomenti per mandare dati in una route
            TestScreen(navController, args) //screen composables from step 3
        }

        composable<Screens.Login> {
            LoginScreen(navController)
        }

        // ogni route è definita come segue
        // composable<Screens.Message> {
        //     val args = it.toRoute<Screens.Message>() // argomenti per mandare dati in una route
        //     Messagescreen(navController, args) //screen composables from step 3
        // }
    }

    content()
}