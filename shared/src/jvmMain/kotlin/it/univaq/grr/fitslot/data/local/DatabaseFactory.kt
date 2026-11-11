package it.univaq.grr.fitslot.data.local

import java.io.File

actual fun percorsoDatabasePredefinito(): String {
    System.getenv("FITSLOT_DB_PATH")?.takeIf { it.isNotBlank() }?.let {
        return File(it).absolutePath
    }
    val cartella = File(System.getProperty("user.home"), ".fitslot")
    check(cartella.isDirectory || cartella.mkdirs()) { "Impossibile creare la cartella del database: $cartella" }
    return File(cartella, "fitslot.db").absolutePath
}

/** Delega al builder condiviso dal team senza duplicare la connessione. */
fun creaDatabase(percorso: String? = null): AppDatabase =
    AppDatabase.create()
