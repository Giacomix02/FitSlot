package it.univaq.grr.fitslot.integrationTest

import it.univaq.grr.fitslot.Vals
import it.univaq.grr.fitslot.creaDbInMemoria
import it.univaq.grr.fitslot.data.repository.FasciaOrariaRepositoryImpl
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FasciaOrariaRepositoryTest {
    val database = creaDbInMemoria()
    val fasciaOrariaRepository = FasciaOrariaRepositoryImpl(database.fasciaOrariaDao())

    @Test
    fun testInsert() {
        assertEquals(1L, runBlocking{
            fasciaOrariaRepository.createFasciaOraria(
                Vals.fasceOrarie[1]
            )
        }, "Inserimento FasciaOraria da repository non riuscito")

        runBlocking {
            assertEquals(Vals.fasceOrarie[1],
                fasciaOrariaRepository.observeFasceOrarie()
                    .first().find { fasciaOraria -> fasciaOraria.id == 1L })
        }
    }

    @Test
    fun testUpdate() {

        runBlocking{
            fasciaOrariaRepository.createFasciaOraria(
                Vals.fasceOrarie[1]
            )

            val updatedFasciaOraria = FasciaOraria(1, LocalDateTime.parse("2026-12-01T07:00:00"), LocalDateTime.parse("2026-12-01T09:00:00"))
            fasciaOrariaRepository.editFasciaOraria(updatedFasciaOraria)

            val fetched = fasciaOrariaRepository.observeFasceOrarie().first()
            assertEquals(updatedFasciaOraria,
                fetched.find { fasciaOraria -> fasciaOraria.id == 1L },
                "Fascia oraria non viene modificata da repository"
            )
        }
    }

    @Test
    fun testDelete() {
        runBlocking {
            fasciaOrariaRepository.createFasciaOraria(Vals.fasceOrarie[1])
            fasciaOrariaRepository.deleteFasciaOraria(Vals.fasceOrarie[1])

            val fetched = fasciaOrariaRepository.observeFasceOrarie().first()
            assertNull(
                fetched.find { fasciaOraria -> fasciaOraria.id == 1L },
                "Fascia oraria non viene eliminata da repository"
            )
        }
    }

}