package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.AppDatabaseConstructor
import it.univaq.grr.fitslot.data.local.entity.CorsoEntity
import it.univaq.grr.fitslot.data.local.entity.SalaEntity
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.data.repository.CorsoRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Verifica i controlli di disponibilità di una sala nelle fasce orarie (CorsoDao). */
class CorsoDisponibilitaTest {

    private var database: AppDatabase? = null

    private val mattina = FasciaOraria(
        1,
        LocalDateTime.parse("2027-06-01T08:00"),
        LocalDateTime.parse("2027-06-01T10:00")
    )
    private val successiva = FasciaOraria(
        2,
        LocalDateTime.parse("2027-06-01T10:00"),
        LocalDateTime.parse("2027-06-01T12:00")
    )
    private val sovrapposta = FasciaOraria(
        3,
        LocalDateTime.parse("2027-06-01T09:00"),
        LocalDateTime.parse("2027-06-01T11:00")
    )
    private val altroGiorno = FasciaOraria(
        4,
        LocalDateTime.parse("2027-06-02T08:00"),
        LocalDateTime.parse("2027-06-02T10:00")
    )

    @BeforeTest
    fun creaDbPerTesting() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>(
            factory = { AppDatabaseConstructor.initialize() })
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    @AfterTest
    fun resettaDbPerTesting() {
        database?.close()
    }

    @Test
    fun salaOccupataSoloConFasceSovrapposte() = runBlocking<Unit> {
        val db = database!!
        val dao = db.corsoDao()
        dao.insertFasceOrarie(*listOf(mattina, successiva, sovrapposta, altroGiorno).map { it.toEntity() }.toTypedArray())

        val salaId = db.salaDao().inserisci(SalaEntity(nome = "Sala 1", capienza = 20))
        val altraSalaId = db.salaDao().inserisci(SalaEntity(nome = "Sala 2", capienza = 20))
        dao.insertAll(CorsoEntity(name = "Yoga", salaAssegnataId = salaId, fasciaOrariaId = mattina.id))

        assertTrue(dao.contaCorsiSovrapposti(salaId, mattina.orarioInizio.toString(), mattina.orarioFine.toString()) > 0)
        // 09:00-11:00 si sovrappone a 08:00-10:00
        assertTrue(dao.contaCorsiSovrapposti(salaId, sovrapposta.orarioInizio.toString(), sovrapposta.orarioFine.toString()) > 0)
        // 10:00-12:00 inizia quando l'altra finisce: nessun conflitto
        assertEquals(0, dao.contaCorsiSovrapposti(salaId, successiva.orarioInizio.toString(), successiva.orarioFine.toString()))
        assertEquals(0, dao.contaCorsiSovrapposti(altraSalaId, mattina.orarioInizio.toString(), mattina.orarioFine.toString()))
        assertEquals(0, dao.contaCorsiSovrapposti(salaId, altroGiorno.orarioInizio.toString(), altroGiorno.orarioFine.toString()))
    }

    @Test
    fun saleEFasceOccupateRestituiteAlDao() = runBlocking<Unit> {
        val db = database!!
        val dao = db.corsoDao()
        dao.insertFasceOrarie(*listOf(mattina, successiva, sovrapposta, altroGiorno).map { it.toEntity() }.toTypedArray())

        val salaId = db.salaDao().inserisci(SalaEntity(nome = "Sala 1", capienza = 20))
        val altraSalaId = db.salaDao().inserisci(SalaEntity(nome = "Sala 2", capienza = 20))
        dao.insertAll(
            CorsoEntity(name = "Yoga", salaAssegnataId = salaId, fasciaOrariaId = mattina.id),
            CorsoEntity(name = "Pilates", salaAssegnataId = altraSalaId, fasciaOrariaId = successiva.id)
        )

        assertEquals(
            setOf(salaId, altraSalaId),
            dao.saleOccupateInFascia(sovrapposta.orarioInizio.toString(), sovrapposta.orarioFine.toString()).toSet()
        )
        assertEquals(
            listOf(mattina.id),
            dao.fasceOrarieOccupatePerSala(salaId).map { it.id }
        )
        assertTrue(dao.fasceOrarieOccupatePerSala(999).isEmpty())
    }

    @Test
    fun repositoryImpedisceDoppiaPrenotazione() = runBlocking<Unit> {
        val db = database!!
        val repository = CorsoRepositoryImpl(db.corsoDao())
        repository.salvaFasceOrarie(listOf(mattina, sovrapposta))

        val salaId = db.salaDao().inserisci(SalaEntity(nome = "Sala 1", capienza = 20))
        val altraSalaId = db.salaDao().inserisci(SalaEntity(nome = "Sala 2", capienza = 20))

        repository.creaCorso(Corso(name = "Yoga", salaAssegnata = Sala(salaId, "Sala 1", 20), fasciaOraria = mattina))

        assertTrue(repository.salaOccupata(salaId, mattina))
        assertTrue(repository.salaOccupata(salaId, sovrapposta))
        assertFalse(repository.salaOccupata(altraSalaId, mattina))
        assertEquals(setOf(salaId), repository.saleOccupate(sovrapposta))
        assertEquals(listOf(mattina), repository.fasceOccupate(salaId))

        val errore = assertFailsWith<IllegalStateException> {
            repository.creaCorso(Corso(name = "Pilates", salaAssegnata = Sala(salaId, "Sala 1", 20), fasciaOraria = sovrapposta))
        }
        assertTrue(errore.message!!.contains("occupata"))

        // Un'altra sala resta disponibile nella stessa fascia.
        repository.creaCorso(Corso(name = "Pilates", salaAssegnata = Sala(altraSalaId, "Sala 2", 20), fasciaOraria = mattina))
        assertEquals(2, db.corsoDao().getALl().size)
    }
}
