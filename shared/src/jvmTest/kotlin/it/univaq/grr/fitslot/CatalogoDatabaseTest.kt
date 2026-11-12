package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import it.univaq.grr.fitslot.data.local.*
import it.univaq.grr.fitslot.data.local.entity.*
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.repository.*
import it.univaq.grr.fitslot.domain.models.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*

/** Verifica i vincoli delle US contro SQLite reale, senza toccare i dati locali. */
class CatalogoDatabaseTest {
    @BeforeTest fun preparaGestore() { impostaUtenteGestioneTest() }
    @AfterTest fun pulisciUtente() { impostaUtenteGestioneTest(null) }

    private fun database(path: String? = null): AppDatabase {
        val builder = if (path == null) Room.inMemoryDatabaseBuilder<AppDatabase>(
            factory = { AppDatabaseConstructor.initialize() })
        else Room.databaseBuilder<AppDatabase>(path, factory = { AppDatabaseConstructor.initialize() })
        return builder.addMigrations(MIGRAZIONE_1_2, MIGRAZIONE_2_3)
            .setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
    }

    private fun utente(ruolo: Ruolo = Ruolo.GESTORE, email: String = "gestore@test.it") = UserEntity(
        email = email, password = "hash", salt = "salt", username = email, nome = "Roberto",
        cognome = "", dataNascita = "", telefono = "", indirizzo = "", citta = "",
        emailContatto = email, ruolo = ruolo)


    @Test fun creazioneModificaEliminazioneAbbonamento() = runBlocking<Unit> {
        val db = database()
        try {
            val repo = CatalogoRepositoryImpl(db.catalogoDao())
            val id = repo.salvaAbbonamento(Abbonamento(nome = "Mensile", prezzoCentesimi = 4990, durataGiorni = 30))
            assertEquals(4990L, repo.osservaAbbonamenti().first().single().prezzoCentesimi)
            repo.salvaAbbonamento(Abbonamento(id, "Trimestrale", 12000, 90))
            assertEquals(Abbonamento(id, "Trimestrale", 12000, 90), repo.osservaAbbonamenti().first().single())
            repo.eliminaAbbonamento(id)
            assertTrue(repo.osservaAbbonamenti().first().isEmpty())
        } finally { db.close() }
    }

    @Test fun pianoAPuntiSalvaIlNumeroDiIngressi() = runBlocking<Unit> {
        val db = database()
        try {
            val repo = CatalogoRepositoryImpl(db.catalogoDao())
            val id = repo.salvaPianoAPunti(PianoAPunti(nome = "Dieci ingressi", prezzoCentesimi = 7000, ingressiInclusi = 10))
            assertEquals(10, repo.osservaPianiAPunti().first().single().ingressiInclusi)
            repo.salvaPianoAPunti(PianoAPunti(id, "Venti ingressi", 12000, 20))
            assertEquals(20, repo.osservaPianiAPunti().first().single().ingressiInclusi)
            repo.eliminaPianoAPunti(id)
            assertTrue(repo.osservaPianiAPunti().first().isEmpty())
        } finally { db.close() }
    }

    @Test fun nomiDuplicatiBloccatiAncheInModifica() = runBlocking<Unit> {
        val db = database()
        try {
            val repo = CatalogoRepositoryImpl(db.catalogoDao())
            val a = repo.salvaAbbonamento(Abbonamento(nome = "Mensile", prezzoCentesimi = 1, durataGiorni = 30))
            val b = repo.salvaAbbonamento(Abbonamento(nome = "Annuale", prezzoCentesimi = 2, durataGiorni = 365))
            assertFailsWith<IllegalArgumentException> { repo.salvaAbbonamento(Abbonamento(nome = "  MENSILE  ", prezzoCentesimi = 3, durataGiorni = 1)) }
            assertFailsWith<IllegalArgumentException> { repo.salvaAbbonamento(Abbonamento(b, "mensile", 3, 1)) }
            repo.salvaAbbonamento(Abbonamento(a, "Mensile", 100, 30))
            assertEquals(2, repo.osservaAbbonamenti().first().size)
            val p = repo.salvaPianoAPunti(PianoAPunti(nome = "Ingressi", prezzoCentesimi = 1, ingressiInclusi = 10))
            val q = repo.salvaPianoAPunti(PianoAPunti(nome = "Extra", prezzoCentesimi = 1, ingressiInclusi = 20))
            assertFailsWith<IllegalArgumentException> { repo.salvaPianoAPunti(PianoAPunti(nome = " INGRESSI ", prezzoCentesimi = 1, ingressiInclusi = 1)) }
            assertFailsWith<IllegalArgumentException> { repo.salvaPianoAPunti(PianoAPunti(q, "ingressi", 1, 1)) }
            repo.salvaPianoAPunti(PianoAPunti(p, "Ingressi", 200, 10))
            assertEquals(2, repo.osservaPianiAPunti().first().size)
        } finally { db.close() }
    }

    @Test fun pianiConIscrittiNonSiPossonoEliminare() = runBlocking<Unit> {
        val db = database()
        try {
            val dao = db.catalogoDao()
            val repo = CatalogoRepositoryImpl(dao)
            val user = 1L
            // Dato di prova inserito senza estendere il DAO utenti del collega.
            db.useWriterConnection { connection ->
                connection.usePrepared("INSERT INTO user (id,email,password,salt,username,nome,cognome,data_nascita,telefono,indirizzo,citta,email_contatto,ruolo) VALUES (1,'iscritto@test.it','hash','salt','iscritto','Iscritto','','','','','','','ISCRITTO')") {
                    it.step()
                }
            }
            val a = repo.salvaAbbonamento(Abbonamento(nome = "Mensile", prezzoCentesimi = 1, durataGiorni = 30))
            val p = repo.salvaPianoAPunti(PianoAPunti(nome = "Ingressi", prezzoCentesimi = 1, ingressiInclusi = 10))
            dao.associaAbbonamento(IscrizioneAbbonamentoEntity(user, a))
            dao.associaPianoAPunti(IscrizionePianoAPuntiEntity(user, p))
            assertFailsWith<IllegalStateException> { repo.eliminaAbbonamento(a) }
            assertFailsWith<IllegalStateException> { repo.eliminaPianoAPunti(p) }
            assertEquals(0, dao.cancellaAbbonamentoSeLibero(a))
            assertEquals(0, dao.cancellaPianoAPuntiSeLibero(p))
            assertNotNull(dao.leggiAbbonamento(a))
            assertNotNull(dao.leggiPianoAPunti(p))
        } finally { db.close() }
    }

    @Test fun iscrizioniRichiedonoUtentiEPianiEsistenti() = runBlocking<Unit> {
        val db = database()
        try {
            assertFails { db.catalogoDao().associaAbbonamento(IscrizioneAbbonamentoEntity(999, 999)) }
            assertFails { db.catalogoDao().associaPianoAPunti(IscrizionePianoAPuntiEntity(999, 999)) }
        } finally { db.close() }
    }

    @Test fun validazioneCampiERecordScomparsi() = runBlocking<Unit> {
        val db = database()
        try {
            val repo = CatalogoRepositoryImpl(db.catalogoDao())
            assertFailsWith<IllegalArgumentException> { repo.salvaAbbonamento(Abbonamento(nome = " ", prezzoCentesimi = 1, durataGiorni = 1)) }
            assertFailsWith<IllegalArgumentException> { repo.salvaAbbonamento(Abbonamento(nome = "A", prezzoCentesimi = -1, durataGiorni = 1)) }
            assertFailsWith<IllegalArgumentException> { repo.salvaAbbonamento(Abbonamento(nome = "A", prezzoCentesimi = 1, durataGiorni = 0)) }
            assertFailsWith<IllegalArgumentException> { repo.salvaPianoAPunti(PianoAPunti(nome = " ", prezzoCentesimi = 1, ingressiInclusi = 1)) }
            assertFailsWith<IllegalArgumentException> { repo.salvaPianoAPunti(PianoAPunti(nome = "A", prezzoCentesimi = -1, ingressiInclusi = 1)) }
            assertFailsWith<IllegalArgumentException> { repo.salvaPianoAPunti(PianoAPunti(nome = "A", prezzoCentesimi = 1, ingressiInclusi = 0)) }
            assertFailsWith<IllegalStateException> { repo.salvaAbbonamento(Abbonamento(999, "A", 1, 1)) }
            assertFailsWith<IllegalStateException> { repo.salvaPianoAPunti(PianoAPunti(999, "P", 1, 1)) }
            assertFailsWith<IllegalStateException> { repo.eliminaAbbonamento(999) }
            assertFailsWith<IllegalStateException> { repo.eliminaPianoAPunti(999) }
        } finally { db.close() }
    }

    @Test fun prezziSenzaArrotondamentiEOOverflow() {
        assertEquals(1234L, prezzoInCentesimi("12,34"))
        assertEquals(1230L, prezzoInCentesimi("12.3"))
        assertEquals(0L, prezzoInCentesimi("0"))
        assertEquals(100L, prezzoInCentesimi(" 1 "))
        for (valore in listOf("", "abc", "-1", "1,234", "1.2.3", "99999999999999999999")) assertNull(prezzoInCentesimi(valore))
        assertEquals(Long.MAX_VALUE, prezzoInCentesimi("92233720368547758.07"))
        assertNull(prezzoInCentesimi("92233720368547758.08"))
        assertEquals("12,34", formattaPrezzo(1234))
    }

    @Ignore
    @Test fun migrazioneDalloSchemaOriginaleConservaLaSala() = runBlocking<Unit> {
        val path = Files.createTempFile("fitslot-migrazione-", ".db")
        try {
            val schema = Json.parseToJsonElement(Files.readString(Path.of("schemas/it.univaq.grr.fitslot.data.local.AppDatabase/1.json")))
                .jsonObject.getValue("database").jsonObject
            val connection = BundledSQLiteDriver().open(path.toString())
            try {
                for (entity in schema.getValue("entities").jsonArray) {
                    val obj = entity.jsonObject
                    connection.execSQL(obj.getValue("createSql").jsonPrimitive.content
                        .replace("\${TABLE_NAME}", obj.getValue("tableName").jsonPrimitive.content))
                }
                for (sql in schema.getValue("setupQueries").jsonArray) connection.execSQL(sql.jsonPrimitive.content)
                connection.execSQL("INSERT INTO sale(id, nome, capienza) VALUES (42, 'Sala originale', 30)")
                connection.execSQL("PRAGMA user_version = 1")
            } finally { connection.close() }
            val db = database(path.toString())
            try {
                assertNull(db.userDao().getUserById(1))
                val repo = CatalogoRepositoryImpl(db.catalogoDao())
                repo.salvaAbbonamento(Abbonamento(nome = "Migrato", prezzoCentesimi = 1200, durataGiorni = 30))
            } finally { db.close() }
            val check = BundledSQLiteDriver().open(path.toString())
            try {
                val stmt = check.prepare("SELECT nome, capienza FROM sale WHERE id = 42")
                try { assertTrue(stmt.step()); assertEquals("Sala originale", stmt.getText(0)); assertEquals(30, stmt.getLong(1).toInt()) }
                finally { stmt.close() }
            } finally { check.close() }
            val reopened = database(path.toString())
            try { assertEquals("Migrato", reopened.catalogoDao().osservaAbbonamenti().first().single().nome) }
            finally { reopened.close() }
        } finally {
            Files.deleteIfExists(path)
            Files.deleteIfExists(Path.of("$path-wal"))
            Files.deleteIfExists(Path.of("$path-shm"))
        }
    }
}
