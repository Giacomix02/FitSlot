package it.univaq.grr.fitslot

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.AppDatabaseConstructor
import it.univaq.grr.fitslot.data.local.entity.*
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.repository.*
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.models.Ruolo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.After
import kotlin.test.assertEquals

/** Prova le azioni delle schermate Compose con repository e SQLite reali. */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CatalogoUiTest {
    @get:Rule val rule = createComposeRule()

    // Navigation Desktop verifica che le transizioni avvengano sul thread Swing.
    @Before fun configuraMain() { Dispatchers.setMain(Dispatchers.Swing) }
    @After fun ripristinaMain() { Dispatchers.resetMain() }

    private fun database() = Room.inMemoryDatabaseBuilder<AppDatabase>(
        factory = { AppDatabaseConstructor.initialize() })
        .setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()

    private fun avvia(db: AppDatabase, ruolo: Ruolo = Ruolo.GESTORE) {
        val user = UserEntity(email = "ui@test.it", password = "hash", salt = "salt", username = "ui",
            nome = "Roberto", cognome = "", dataNascita = "", telefono = "", indirizzo = "", citta = "",
            emailContatto = "ui@test.it", ruolo = ruolo)
        val id = runBlocking { db.userDao().inserisci(user) }
        val sessione = SessioneUtente().also { it.accedi(user.copy(id = id).toDomain()) }
        rule.setContent {
            App(SalaRepositoryImpl(db.salaDao(), sessione), UserRepositoryImpl(db.userDao()),
                CatalogoRepositoryImpl(db.catalogoDao(), sessione), sessione)
        }
        attendi("FitSlot")
    }

    private fun attendi(testo: String) {
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithText(testo).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun campo(nome: String, valore: String) {
        rule.onNodeWithText(nome).performScrollTo().performTextReplacement(valore)
    }

    private fun SemanticsNodeInteraction.clicca(): SemanticsNodeInteraction {
        performSemanticsAction(SemanticsActions.OnClick) { action ->
            rule.runOnUiThread { action() }
        }
        rule.waitForIdle()
        return this
    }

    @Test fun gestoreCreaModificaEliminaEntrambiITipiDiPiano() {
        val db = database()
        try {
            avvia(db)
            for ((pulsante, quantita, nome) in listOf(
                Triple("Gestisci abbonamenti", "Durata in giorni", "Mensile UI"),
                Triple("Gestisci piani a punti", "Ingressi inclusi", "Ingressi UI")
            )) {
                rule.onNodeWithText(pulsante).clicca()
                attendi("Nuovo piano")
                campo("Nome", nome)
                campo("Prezzo (€)", "29,90")
                campo(quantita, "30")
                rule.onNodeWithText("Crea piano").performScrollTo().clicca()
                attendi("Piano creato e disponibile per la sottoscrizione.")
                rule.onNodeWithText("Modifica").performScrollTo().clicca()
                campo("Nome", "$nome aggiornato")
                campo("Prezzo (€)", "39,90")
                rule.onNodeWithText("Salva modifiche").performScrollTo().clicca()
                attendi("Modifiche salvate correttamente.")
                rule.onNodeWithText("$nome aggiornato").performScrollTo().assertIsDisplayed()
                rule.onNodeWithText("Elimina").performScrollTo().clicca()
                rule.onNodeWithText("Annulla").clicca()
                rule.onNodeWithText("$nome aggiornato").assertExists()
                rule.onNodeWithText("Elimina").performScrollTo().clicca()
                rule.onAllNodesWithText("Elimina").onLast().clicca()
                attendi("Piano eliminato correttamente.")
                rule.onNodeWithText("$nome aggiornato").assertDoesNotExist()
                rule.onNodeWithText("Torna alla Home").performScrollTo().clicca()
                attendi("FitSlot")
            }
            assertEquals(0, runBlocking { db.catalogoDao().osservaAbbonamenti().first().size })
            assertEquals(0, runBlocking { db.catalogoDao().osservaPianiAPunti().first().size })
        } finally { runBlocking { db.close() } }
    }

    @Test fun iscrittoNonVedeLeOperazioniDelGestoreELogoutFunziona() {
        val db = database()
        try {
            avvia(db, Ruolo.ISCRITTO)
            rule.onNodeWithText("Gestisci abbonamenti").assertDoesNotExist()
            rule.onNodeWithText("Gestisci piani a punti").assertDoesNotExist()
            rule.onNodeWithText("Crea sala").assertDoesNotExist()
            rule.onNodeWithText("Esci").clicca()
            attendi("Login")
            rule.onNodeWithText("Benvenuto, Roberto").assertDoesNotExist()
        } finally { runBlocking { db.close() } }
    }

    @Test fun primoAvvioConfigurazioneEAccessoConCredenzialiScelte() {
        val db = database()
        try {
            val sessione = SessioneUtente()
            val utenti = UserRepositoryImpl(db.userDao())
            rule.setContent {
                App(SalaRepositoryImpl(db.salaDao(), sessione), utenti,
                    CatalogoRepositoryImpl(db.catalogoDao(), sessione), sessione)
            }
            attendi("Configura il primo gestore")
            campo("Nome", "Gestore prova")
            campo("Email", "gestore-ui@test.it")
            campo("Password (almeno 8 caratteri)", "ProvaSoloNeiTest123!")
            campo("Conferma password", "ProvaSoloNeiTest123!")
            rule.onNodeWithText("Crea gestore").performScrollTo().clicca()
            attendi("Gestisci abbonamenti")
            rule.onNodeWithText("Esci").clicca()
            attendi("Login")
            rule.onNodeWithText("Login").clicca()
            rule.onNodeWithText("Email").performTextReplacement("gestore-ui@test.it")
            rule.onNodeWithText("Password").performTextReplacement("errata")
            rule.onNodeWithText("Login").clicca()
            attendi("Email o password non validi")
            rule.onNodeWithText("Password").performTextReplacement("ProvaSoloNeiTest123!")
            rule.onNodeWithText("Login").clicca()
            attendi("Gestisci piani a punti")
            rule.onNodeWithText("Benvenuto, Gestore prova").assertExists()
        } finally { runBlocking { db.close() } }
    }
}
