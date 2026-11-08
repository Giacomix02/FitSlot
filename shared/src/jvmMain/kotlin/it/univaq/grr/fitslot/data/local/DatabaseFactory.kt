package it.univaq.grr.fitslot.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers

/** Prepara il database persistete dell'applicazione desktop.
 * L'istanza restituita va conservata e riutilizzata. */

fun creaDatabase(): AppDatabase {
    //Conserva i dati fuori dalla cartella del progetto
    val cartellaDatabase = File(
        System.getProperty("user.home"),
        ".fitslot"
    )

    //crea la cartella se manca e segnala eventuali problemi.
    check(cartellaDatabase.isDirectory || cartellaDatabase.mkdirs()) {
        "impossibile creare la cartella del database: $cartellaDatabase"
    }

    val fileDatabase = File(cartellaDatabase, "fitslot.db")

    return Room.databaseBuilder<AppDatabase>(
        name = fileDatabase.absolutePath,
        factory = { AppDatabaseConstructor.initialize() }
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}