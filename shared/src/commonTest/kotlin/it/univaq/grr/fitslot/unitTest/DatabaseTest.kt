package it.univaq.grr.fitslot.unitTest

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

fun creaDbInMemoria(): AppDatabase {
    return Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()
}

open class DatabaseTest {

    var database: AppDatabase? = null

    @BeforeTest
    fun creaDbPerTesting() {
        database = creaDbInMemoria()
    }

    @AfterTest
    fun resettaDbPerTesting() {
        database?.close()
    }

    fun <T> testDao(dao: T, type: String) {
        assertNotNull(dao, "DAO ${type} non viene istanziato")
    }

}

