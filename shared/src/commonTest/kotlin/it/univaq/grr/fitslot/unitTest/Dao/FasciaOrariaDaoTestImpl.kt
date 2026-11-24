package it.univaq.grr.fitslot.unitTest.Dao

import it.univaq.grr.fitslot.Vals
import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.unitTest.DatabaseTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

open class FasciaOrariaDaoTestImpl : DatabaseTest() {

    val fasciaOrariaDao get() = database?.fasciaOrariaDao()

    @Test
    fun testDao() = super.testDao(fasciaOrariaDao, "FasciaOrariaDao")

    @Test
    fun testInsert() = runBlocking {
        assertEquals(1L, fasciaOrariaDao?.insertAll(
            Vals.fasceOrarie[1].toEntity()
        ), "Inserimento FasciaOraria in db non riuscito")

        val fetched = fasciaOrariaDao?.observeFasceOrarie()?.first()
        assertEquals(Vals.fasceOrarie[1].toEntity(),
            fetched?.find { fasciaOrariaEntity -> fasciaOrariaEntity.id == 1L })
    }

    @Test
    fun testUpdate() = runBlocking {
        fasciaOrariaDao?.insertAll(
            Vals.fasceOrarie[1].toEntity()
        )

        val updatedFasciaOraria = FasciaOrariaEntity(1, "2026-12-01T07:00:00", "2026-12-01T09:00:00")
        fasciaOrariaDao?.updateFasciaOraria(updatedFasciaOraria)

        val fetched = fasciaOrariaDao?.observeFasceOrarie()?.first()
        assertEquals(updatedFasciaOraria,
            fetched?.find { fasciaOrariaEntity -> fasciaOrariaEntity.id == 1L },
            "Fascia oraria non viene modificata nel db"
        )
    }

    @Test
    fun testDelete() = runBlocking {
        fasciaOrariaDao?.insertAll(Vals.fasceOrarie[1].toEntity())
        fasciaOrariaDao?.deleteALl(Vals.fasceOrarie[1].toEntity())

        val fetched = fasciaOrariaDao?.observeFasceOrarie()?.first()
        assertNull(
            fetched?.find { fasciaOrariaEntity -> fasciaOrariaEntity.id == 1L },
            "Fascia oraria non viene eliminata nel db"
        )
    }


}