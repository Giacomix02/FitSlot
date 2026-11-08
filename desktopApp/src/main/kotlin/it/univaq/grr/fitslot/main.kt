package it.univaq.grr.fitslot

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import it.univaq.grr.fitslot.data.local.creaDatabase
import it.univaq.grr.fitslot.data.repository.*
import it.univaq.grr.fitslot.domain.SessioneUtente
import kotlinx.coroutines.runBlocking

/** Prepara una sola sessione e un solo database per tutta l'applicazione. */
fun main() {
    val database = creaDatabase(System.getenv("FITSLOT_DB_PATH"))
    try {
        val sessione = SessioneUtente()
        val salaRepository = SalaRepositoryImpl(database.salaDao(), sessione)
        val userRepository = UserRepositoryImpl(database.userDao())
        val catalogoRepository = CatalogoRepositoryImpl(database.catalogoDao(), sessione)
        application {
            Window(onCloseRequest = ::exitApplication, title = "FitSlot") {
                App(salaRepository, userRepository, catalogoRepository, sessione)
            }
        }
    } finally {
        runBlocking { database.close() }
    }
}
