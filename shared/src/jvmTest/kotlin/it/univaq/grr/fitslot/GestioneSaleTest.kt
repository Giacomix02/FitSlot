package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.*
import it.univaq.grr.fitslot.data.local.entity.CorsoEntity
import it.univaq.grr.fitslot.data.repository.*
import it.univaq.grr.fitslot.domain.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.test.*

/** Controlla i vincoli della GRR-14 su SQLite reale, senza usare il database personale. */
class GestioneSaleTest {
    @BeforeTest fun preparaGestore() { impostaUtenteGestioneTest() }
    @AfterTest fun pulisciUtente() { impostaUtenteGestioneTest(null) }

    private fun database() = Room.inMemoryDatabaseBuilder<AppDatabase>(
        factory = { AppDatabaseConstructor.initialize() })
        .setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()

    @Test fun creaModificaEliminaSalaLibera() = runBlocking<Unit> {
        val db = database()
        try {
            val repo = SalaRepositoryImpl(db.salaDao())
            val id = repo.inserisci(Sala(nome = " Sala pesi ", capienza = 30))
            assertEquals("Sala pesi", repo.osservaSale().first().single().nome)
            repo.modifica(Sala(id, "Sala corsi", 45))
            val sala = repo.osservaSale().first().single()
            assertEquals(id, sala.id)
            assertEquals("Sala corsi", sala.nome)
            assertEquals(45, sala.capienza)
            repo.elimina(id)
            assertTrue(repo.osservaSale().first().isEmpty())
        } finally { db.close() }
    }

    @Test fun salaConCorsoSiPuoModificareMaNonEliminare() = runBlocking<Unit> {
        val db = database()
        try {
            val repo = SalaRepositoryImpl(db.salaDao())
            val id = repo.inserisci(Sala(nome = "Sala usata", capienza = 30))
            db.corsoDao().insertAll(CorsoEntity(name = "Corso", salaAssegnataId = id, fasciaOrariaId = 1))
            repo.modifica(Sala(id, "Sala rinominata", 35))
            assertEquals(id, db.corsoDao().getALl().single().salaAssegnataId)
            val errore = assertFailsWith<IllegalStateException> { repo.elimina(id) }
            assertTrue(errore.message!!.contains("assegnata"))
            assertNotNull(db.salaDao().leggi(id))
            assertEquals(0, db.salaDao().cancellaSeLibera(id))
            db.corsoDao().delete(db.corsoDao().getALl().single())
            repo.elimina(id)
            assertNull(db.salaDao().leggi(id))
        } finally { db.close() }
    }

    @Test fun validazioneENessunaModificaDiRecordMancanti() = runBlocking<Unit> {
        val db = database()
        try {
            val repo = SalaRepositoryImpl(db.salaDao())
            assertFailsWith<IllegalArgumentException> { repo.inserisci(Sala(nome = " ", capienza = 10)) }
            assertFailsWith<IllegalArgumentException> { repo.inserisci(Sala(nome = "Sala", capienza = 0)) }
            val id = repo.inserisci(Sala(nome = "Sala", capienza = 10))
            assertFailsWith<IllegalArgumentException> { repo.modifica(Sala(id, "", 10)) }
            assertFailsWith<IllegalArgumentException> { repo.modifica(Sala(id, "Sala", -1)) }
            assertEquals(10, db.salaDao().leggi(id)!!.capienza)
            assertFailsWith<IllegalStateException> { repo.modifica(Sala(999, "Sala", 10)) }
            assertFailsWith<IllegalStateException> { repo.elimina(999) }
        } finally { db.close() }
    }

    @Test fun soloGestorePuoScrivereNelleTreUS() = runBlocking<Unit> {
        val db = database()
        try {
            val sale = SalaRepositoryImpl(db.salaDao())
            val catalogo = CatalogoRepositoryImpl(db.catalogoDao())
            val salaId = sale.inserisci(Sala(nome = "Sala", capienza = 10))
            val a = catalogo.salvaAbbonamento(Abbonamento(nome = "Mensile", prezzoCentesimi = 100, durataGiorni = 30))
            val p = catalogo.salvaPianoAPunti(PianoAPunti(nome = "Ingressi", prezzoCentesimi = 100, ingressiInclusi = 10))
            for (ruolo in listOf(null, Ruolo.ISCRITTO, Ruolo.ALLENATORE, Ruolo.RECEPIONIST)) {
                impostaUtenteGestioneTest(ruolo)
                assertFalse(PermessiGestioneDaLocalStorage.puoGestire())
                assertFailsWith<IllegalStateException> { sale.inserisci(Sala(nome = "Vietata", capienza = 1)) }
                assertFailsWith<IllegalStateException> { sale.modifica(Sala(salaId, "Vietata", 1)) }
                assertFailsWith<IllegalStateException> { sale.elimina(salaId) }
                assertFailsWith<IllegalStateException> { catalogo.salvaAbbonamento(Abbonamento(a, "Vietato", 1, 1)) }
                assertFailsWith<IllegalStateException> { catalogo.salvaPianoAPunti(PianoAPunti(p, "Vietato", 1, 1)) }
                assertFailsWith<IllegalStateException> { catalogo.eliminaAbbonamento(a) }
                assertFailsWith<IllegalStateException> { catalogo.eliminaPianoAPunti(p) }
            }
            assertEquals("Sala", db.salaDao().leggi(salaId)!!.nome)
            assertEquals("Mensile", db.catalogoDao().leggiAbbonamento(a)!!.nome)
            assertEquals("Ingressi", db.catalogoDao().leggiPianoAPunti(p)!!.nome)
            impostaUtenteGestioneTest()
            assertTrue(PermessiGestioneDaLocalStorage.puoGestire())
            sale.elimina(salaId)
            catalogo.eliminaAbbonamento(a)
            catalogo.eliminaPianoAPunti(p)
        } finally { db.close() }
    }
}
