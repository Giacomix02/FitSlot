package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import java.time.LocalDateTime

fun creaDbInMemoria(): AppDatabase {
    return Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()
}


object Vals {
    val sala = Sala(0, "salaTest", 1)

    val fasceOrarie = listOf(
        FasciaOraria(
            0,
            LocalDateTime.parse("2087-10-23T08:00:00"),
            LocalDateTime.parse("2087-10-23T10:00:00")
        ),
        FasciaOraria(
            1,
            LocalDateTime.parse("2087-10-23T14:00:00"),
            LocalDateTime.parse("2087-10-23T16:00:00")
        )
    )
}

