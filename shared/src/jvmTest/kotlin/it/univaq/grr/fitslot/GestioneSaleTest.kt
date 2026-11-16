package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.AppDatabaseConstructor
import it.univaq.grr.fitslot.data.local.entity.CorsoEntity
import it.univaq.grr.fitslot.data.repository.SalaRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Sala
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Controlla i vincoli della GRR-14 su SQLite reale, senza usare il database personale. */
class GestioneSaleTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: SalaRepositoryImpl

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
        repo = SalaRepositoryImpl(db.salaDao())
    }

    @AfterTest
    fun chiudiDatabase() {
        db.close()
        impostaUtenteGestioneTest(null)
    }

    @Test
    fun creaModificaEliminaSalaLibera() = runBlocking<Unit> {
        val id = repo.inserisci(Sala(nome = " Sala pesi ", capienza = 30))
        val sale = repo.osservaSale().first()
        assertEquals("Sala pesi", sale[0].nome)

        repo.modifica(Sala(id, "Sala corsi", 45))
        val aggiornate = repo.osservaSale().first()
        val sala = aggiornate[0]
        assertEquals(id, sala.id)
        assertEquals("Sala corsi", sala.nome)
        assertEquals(45, sala.capienza)

        repo.elimina(id)
        assertTrue(repo.osservaSale().first().isEmpty())
    }

    @Test
    fun nonEliminaUnaSalaUsataDaUnCorso() = runBlocking<Unit> {
        val id = repo.inserisci(Sala(nome = "Sala corsi", capienza = 30))
        db.corsoDao().insertAll(CorsoEntity(name = "Yoga", salaAssegnataId = id, fasciaOrariaId = 1))

        assertFailsWith<IllegalStateException> {
            repo.elimina(id)
        }
        assertNotNull(db.salaDao().leggi(id))
    }

}
