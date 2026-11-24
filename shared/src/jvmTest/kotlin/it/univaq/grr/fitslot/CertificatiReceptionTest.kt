package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.entity.CertificatoMedicoEntity
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Ruolo
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class CertificatiReceptionTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: CertificatoMedicoRepositoryImpl
    private val oggi = "2026-11-20"

    @BeforeTest
    fun prepara() = runBlocking<Unit> {
        impostaUtenteGestioneTest(Ruolo.RECEPIONIST)
        db = creaDbInMemoria()
        repository = CertificatoMedicoRepositoryImpl(db.certificatoMedicoDao())
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
        val elenco = repository.certificati(true, oggi, oggi, oggi)
        assertEquals(listOf("Anna"), elenco.map { it.utente.nome })
        assertEquals("2026-11-19", elenco[0].scadenza)
    }

    @Test
    fun mostraNonScadutiNellIntervallo() = runBlocking<Unit> {
        assertEquals(listOf("Luca"), repository.certificati(false, oggi, oggi, oggi).map { it.utente.nome })
        assertTrue(repository.certificati(false, "2026-11-21", "2026-11-22", oggi).isEmpty())
    }

    @Test
    fun certificatoRealeDelSoloIscrittoScelto() = runBlocking<Unit> {
        val certificati = CertificatoMedicoRepositoryImpl(db.certificatoMedicoDao())
        val anna = repository.iscritti().first { it.id == 1L }
        val sara = repository.iscritti().first { it.id == 3L }
        assertEquals("2026-11-19", certificati.getCertificatoMedicoByUser(anna)?.scadenza)
        assertNull(certificati.getCertificatoMedicoByUser(sara))
        db.certificatoMedicoDao().insert(CertificatoMedicoEntity(path = "rinnovo", scadenza = "2027-01-01", userId = 1))
        assertEquals("rinnovo", certificati.getCertificatoMedicoByUser(anna)?.path)
    }

    @Test
    fun prendeLaScadenzaMaggioreNonLIdMaggiore() = runBlocking<Unit> {
        db.certificatoMedicoDao().insert(CertificatoMedicoEntity(path = "più recente", scadenza = "1798761600000", userId = 1))
        db.certificatoMedicoDao().insert(CertificatoMedicoEntity(path = "vecchio", scadenza = "2026-11-01", userId = 1))
        assertEquals("più recente", db.certificatoMedicoDao().getByUserId(1)?.path)
    }
}
