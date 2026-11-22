package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.entity.CertificatoMedicoEntity
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.data.repository.CertificatiReceptionRepository
import it.univaq.grr.fitslot.domain.models.Ruolo
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.test.*

class CertificatiReceptionTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: CertificatiReceptionRepository
    private val oggi = LocalDate.parse("2026-11-20")

    @BeforeTest
    fun prepara() = runBlocking<Unit> {
        impostaUtenteGestioneTest(Ruolo.RECEPIONIST)
        db = creaDbInMemoria()
        repository = CertificatiReceptionRepository(db.certificatiReceptionDao(), db.userDao())
        db.userDao().insertUser(UserEntity(1, "uno@test.it", "", "", "uno", "Anna", "Rossi", "", "", "", "", "", Ruolo.ISCRITTO))
        db.userDao().insertUser(UserEntity(2, "due@test.it", "", "", "due", "Luca", "Verdi", "", "", "", "", "", Ruolo.ISCRITTO))
        db.userDao().insertUser(UserEntity(3, "tre@test.it", "", "", "tre", "Sara", "Bianchi", "", "", "", "", "", Ruolo.ISCRITTO))
        db.certificatoMedicoDao().insert(CertificatoMedicoEntity(path = "foto", scadenza = "2026-11-19", userId = 1))
        db.certificatoMedicoDao().insert(CertificatoMedicoEntity(path = "foto", scadenza = "1795132800000", userId = 2))
    }

    @AfterTest
    fun chiudi() {
        db.close()
        impostaUtenteGestioneTest(null)
    }

    @Test
    fun mostraSoloScaduti() = runBlocking<Unit> {
        val elenco = repository.elenco(true, oggi, oggi, oggi)
        assertEquals(listOf("Anna"), elenco.map { it.nome })
        assertEquals("2026-11-19", elenco[0].scadenza)
    }

    @Test
    fun mostraNonScadutiNellIntervallo() = runBlocking<Unit> {
        assertEquals(listOf("Luca"), repository.elenco(false, oggi, oggi, oggi).map { it.nome })
        assertTrue(repository.elenco(false, oggi.plusDays(1), oggi.plusDays(2), oggi).isEmpty())
    }

    @Test
    fun certificatoRealeDelSoloIscrittoScelto() = runBlocking<Unit> {
        val certificati = it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl(db.certificatoMedicoDao())
        val anna = repository.utente(1)
        val sara = repository.utente(3)
        assertEquals("2026-11-19", certificati.getCertificatoMedicoByUser(anna)?.scadenza)
        assertNull(certificati.getCertificatoMedicoByUser(sara))
        db.certificatoMedicoDao().insert(CertificatoMedicoEntity(path = "rinnovo", scadenza = "2027-01-01", userId = 1))
        assertEquals("rinnovo", certificati.getCertificatoMedicoByUser(anna)?.path)
    }

    @Test
    fun elencoRiservatoAllaReception() = runBlocking<Unit> {
        impostaUtenteGestioneTest(Ruolo.ISCRITTO)
        assertFailsWith<IllegalStateException> { repository.iscritti() }
    }
}
