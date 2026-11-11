package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.data.local.AppDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SharedCommonTest {

    /**
     * Db related tests
     */
    @Test
    fun createDbTest() {
        assertNotNull(AppDatabase.create(name = "fitslotTest.db"), "Creazione database null")
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
    fun example() {
        assertEquals(3, 1 + 2)
    }
}