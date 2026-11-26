package it.univaq.grr.fitslot

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import it.univaq.grr.fitslot.data.local.creaDatabase
import it.univaq.grr.fitslot.data.repository.CatalogoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.SalaRepositoryImpl
import kotlinx.coroutines.runBlocking

/** Prepara le dipendenze di sale e piani; login e registrazione restano al team. */
fun main() {
    val database = creaDatabase(System.getenv("FITSLOT_DB_PATH"))
    try {
        val sale = SalaRepositoryImpl(database.salaDao())
        val catalogo = CatalogoRepositoryImpl(database.catalogoDao())
        application {
            Window(onCloseRequest = ::exitApplication, title = "FitSlot") { App(sale, catalogo) }
        }
    } finally {
        runBlocking { database.close() }
    }
}