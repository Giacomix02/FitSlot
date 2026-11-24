package it.univaq.grr.fitslot.unitTest

import it.univaq.grr.fitslot.creaDbInMemoria
import it.univaq.grr.fitslot.data.local.AppDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull


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