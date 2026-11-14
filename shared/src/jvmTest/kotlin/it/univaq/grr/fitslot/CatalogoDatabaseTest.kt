package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.AppDatabaseConstructor
import it.univaq.grr.fitslot.data.local.entity.IscrizioneAbbonamentoEntity
import it.univaq.grr.fitslot.data.local.entity.IscrizionePianoAPuntiEntity
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.data.repository.CatalogoRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.PianoAPunti
import it.univaq.grr.fitslot.domain.models.Ruolo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

/** Verifica i vincoli delle US contro SQLite reale, senza toccare i dati locali. */
class CatalogoDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: CatalogoRepositoryImpl

    // Ogni test parte da un database nuovo e vuoto.
    @BeforeTest
    fun preparaDatabase() {
        impostaUtenteGestioneTest()
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(
            factory = { AppDatabaseConstructor.initialize() }
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        repo = CatalogoRepositoryImpl(db.catalogoDao())
    }

    @AfterTest
    fun chiudiDatabase() {
        db.close()
        impostaUtenteGestioneTest(null)
    }

    @Test
    fun creazioneModificaEliminazioneAbbonamento() = runBlocking<Unit> {
        val piano = Abbonamento(nome = "Mensile", prezzoCentesimi = 4990, durataGiorni = 30)
        val id = repo.salvaAbbonamento(piano)
        assertFailsWith<IllegalArgumentException> {
            repo.salvaAbbonamento(piano)
        }
        val abbonamenti = repo.osservaAbbonamenti().first()
        assertEquals(4990L, abbonamenti[0].prezzoCentesimi)

        repo.salvaAbbonamento(Abbonamento(id, "Trimestrale", 12000, 90))
        val aggiornati = repo.osservaAbbonamenti().first()
        assertEquals("Trimestrale", aggiornati[0].nome)
        assertEquals(90, aggiornati[0].durataGiorni)

        repo.eliminaAbbonamento(id)
        assertTrue(repo.osservaAbbonamenti().first().isEmpty())
    }

    @Test
    fun pianoAPuntiSalvaIlNumeroDiIngressi() = runBlocking<Unit> {
        val piano = PianoAPunti(nome = "Dieci ingressi", prezzoCentesimi = 7000, ingressiInclusi = 10)
        val id = repo.salvaPianoAPunti(piano)
        assertFailsWith<IllegalArgumentException> {
            repo.salvaPianoAPunti(piano)
        }
        val piani = repo.osservaPianiAPunti().first()
        assertEquals(10, piani[0].ingressiInclusi)

        repo.salvaPianoAPunti(PianoAPunti(id, "Venti ingressi", 12000, 20))
        val aggiornati = repo.osservaPianiAPunti().first()
        assertEquals(20, aggiornati[0].ingressiInclusi)

        repo.eliminaPianoAPunti(id)
        assertTrue(repo.osservaPianiAPunti().first().isEmpty())
    }

    @Test
    fun pianiConIscrittiNonSiPossonoEliminare() = runBlocking<Unit> {
        val dao = db.catalogoDao()
        val utenteId = 1L
        db.userDao().insertUser(UserEntity(
            id = utenteId, email = "iscritto@test.it", password = "hash", salt = "salt",
            username = "iscritto", nome = "Iscritto", cognome = "", dataNascita = "",
            telefono = "", indirizzo = "", citta = "", emailContatto = "", ruolo = Ruolo.ISCRITTO
        ))
        val abbonamentoId = repo.salvaAbbonamento(Abbonamento(nome = "Mensile", prezzoCentesimi = 1, durataGiorni = 30))
        val pianoId = repo.salvaPianoAPunti(PianoAPunti(nome = "Ingressi", prezzoCentesimi = 1, ingressiInclusi = 10))
        dao.associaAbbonamento(IscrizioneAbbonamentoEntity(utenteId, abbonamentoId))
        dao.associaPianoAPunti(IscrizionePianoAPuntiEntity(utenteId, pianoId))
        assertFailsWith<IllegalStateException> {
            repo.eliminaAbbonamento(abbonamentoId)
        }
        assertFailsWith<IllegalStateException> {
            repo.eliminaPianoAPunti(pianoId)
        }
        assertNotNull(dao.leggiAbbonamento(abbonamentoId))
        assertNotNull(dao.leggiPianoAPunti(pianoId))
    }

}
