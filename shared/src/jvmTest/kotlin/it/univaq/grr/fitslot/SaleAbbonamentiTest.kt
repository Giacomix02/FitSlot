package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.AppDatabaseConstructor
import it.univaq.grr.fitslot.data.repository.CatalogoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.SalaRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.Sala
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SaleAbbonamentiTest {
    private lateinit var db: AppDatabase
    private lateinit var catalogo: CatalogoRepositoryImpl
    private lateinit var sale: SalaRepositoryImpl

    @BeforeTest
    fun preparaDatabase() {
        impostaUtenteGestioneTest()
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(factory = { AppDatabaseConstructor.initialize() })
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        catalogo = CatalogoRepositoryImpl(db.catalogoDao())
        sale = SalaRepositoryImpl(db.salaDao())
    }

    @AfterTest
    fun chiudiDatabase() {
        db.close()
        impostaUtenteGestioneTest(null)
    }

    @Test
    fun associaSalaAllAbbonamentoSceltoSenzaDuplicati() = runBlocking<Unit> {
        val salaId = sale.inserisci(Sala(nome = "Sala pesi", capienza = 20))
        val mensile = catalogo.salvaAbbonamento(Abbonamento(nome = "Mensile", prezzoCentesimi = 1000, durataGiorni = 30))
        val annuale = catalogo.salvaAbbonamento(Abbonamento(nome = "Annuale", prezzoCentesimi = 9000, durataGiorni = 365))
        catalogo.associaSala(mensile, salaId)
        assertEquals("Sala pesi", catalogo.saleIncluse(mensile)[0].nome)
        assertTrue(catalogo.saleIncluse(annuale).isEmpty())
        assertFailsWith<IllegalStateException> { catalogo.associaSala(mensile, salaId) }
        assertEquals(1, catalogo.saleIncluse(mensile).size)
    }

    @Test
    fun salaInclusaRestaProtettaFincheEsisteLAbbonamento() = runBlocking<Unit> {
        val salaId = sale.inserisci(Sala(nome = "Sala pesi", capienza = 20))
        val pianoId = catalogo.salvaAbbonamento(Abbonamento(nome = "Mensile", prezzoCentesimi = 1000, durataGiorni = 30))
        catalogo.associaSala(pianoId, salaId)
        assertFailsWith<IllegalStateException> { sale.elimina(salaId) }
        catalogo.eliminaAbbonamento(pianoId)
        assertTrue(catalogo.saleIncluse(pianoId).isEmpty())
        sale.elimina(salaId)
    }
    @Test
    fun associaPiuSaleInsiemeSenzaDuplicarle() = runBlocking<Unit> {
        val prima = sale.inserisci(Sala(nome = "Sala A", capienza = 10))
        val seconda = sale.inserisci(Sala(nome = "Sala B", capienza = 10))
        val piano = catalogo.salvaAbbonamento(Abbonamento(nome = "Completo", prezzoCentesimi = 1000, durataGiorni = 30))
        catalogo.associaSale(piano, listOf(prima, seconda))
        catalogo.associaSale(piano, listOf(prima))
        assertEquals(2, catalogo.saleIncluse(piano).size)
    }

    @Test
    fun associaCorsiAlPianoScelto() = runBlocking<Unit> {
        val sala = sale.inserisci(Sala(nome = "Sala Yoga", capienza = 10))
        val fascia = db.corsoDao().insertFasciaOraria(it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity(1, "2027-06-01T08:00", "2027-06-01T10:00"))
        db.corsoDao().insertAll(it.univaq.grr.fitslot.data.local.entity.CorsoEntity(1, "Yoga", sala, fascia))
        val piano = catalogo.salvaAbbonamento(Abbonamento(nome = "Corsi", prezzoCentesimi = 1000, durataGiorni = 30))
        catalogo.associaCorsi(piano, listOf(1))
        catalogo.associaCorsi(piano, listOf(1))
        assertEquals("Yoga", catalogo.corsiInclusi(piano).single().name)
        catalogo.eliminaAbbonamento(piano)
        assertTrue(catalogo.corsiInclusi(piano).isEmpty())
    }
}
