package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen.formatter
import it.univaq.grr.fitslot.utils.parseCatching
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SharedCommonTest {

    /**
     * Db related tests
     */
    @Test
    fun createDbTest() {
        assertNotNull(AppDatabase.create(), "Creazione database null")
    }
    var database: AppDatabase? = null

    @BeforeTest
    fun creaDbPerTesting(){
        database = creaDbInMemoria()
    }
    @AfterTest
    fun resettaDbPerTesting(){
        database?.close()
    }

    @Test
    fun createCorsoDaoTest(){
        assertNotNull(database?.corsoDao(), "Creazione CorsoDao nulla")
    }
    
    @Test
    fun parseCatchingTest() {
        val invalidDate = "32/11/2026"
        val validDate = "21/11/2026"
        assertNull(parseCatching(invalidDate, formatter))
        assertNotNull(parseCatching(validDate, formatter))
    }
}
