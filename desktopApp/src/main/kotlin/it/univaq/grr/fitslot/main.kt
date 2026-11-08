package it.univaq.grr.fitslot

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import it.univaq.grr.fitslot.data.local.creaDatabase
import it.univaq.grr.fitslot.data.repository.SalaRepositoryImpl
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import kotlinx.coroutines.runBlocking

/** Punto di avvio dell'applicazione desktop.
 * Prepara database e repository, poi apre la finestra. */
fun main() {
    // Il database viene creato una sola volta per esecuzione.
    val database = creaDatabase()

    try {
        // Il repository utilizza il DAO ottenuto dal database.
        val salaRepository = SalaRepositoryImpl(database.salaDao())

        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "FitSlot"
            ) {
                App(salaRepository, UserRepositoryImpl(database.userDao()))
            }
        }
    } finally {
        // Rilascia le risorse quando l'applicazione termina.
        runBlocking {
            database.close()
        }
    }
}
