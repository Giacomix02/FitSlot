package it.univaq.grr.fitslot

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.local.AppDatabaseConstructor
import it.univaq.grr.fitslot.data.repository.*
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
    @Before fun configuraMain() { Dispatchers.setMain(Dispatchers.Swing); impostaUtenteGestioneTest() }
    @After fun ripristinaMain() { Dispatchers.resetMain(); impostaUtenteGestioneTest(null) }

    private fun database() = Room.inMemoryDatabaseBuilder<AppDatabase>(
        factory = { AppDatabaseConstructor.initialize() })
        .setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()

    private fun avvia(db: AppDatabase) {
        rule.setContent { App(SalaRepositoryImpl(db.salaDao()), CatalogoRepositoryImpl(db.catalogoDao()), PermessiGestioneDaLocalStorage) }
        attendi("Screen Home")
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
                attendi("Screen Home")
            }
            assertEquals(0, runBlocking { db.catalogoDao().osservaAbbonamenti().first().size })
            assertEquals(0, runBlocking { db.catalogoDao().osservaPianiAPunti().first().size })
        } finally { runBlocking { db.close() } }
    }


    @Test fun gestioneSaleCompletaConConfermaEBloccoDeiCorsi() {
        val db = database()
        try {
            avvia(db)
            rule.onNodeWithText("Gestisci sale").clicca()
            attendi("Nuova sala")
            campo("Nome", "Sala prova")
            campo("Capienza", "30")
            rule.onNodeWithText("Crea sala").performScrollTo().clicca()
            attendi("Sala creata correttamente.")
            rule.onNodeWithText("Modifica").performScrollTo().clicca()
            campo("Nome", "Sala aggiornata")
            campo("Capienza", "45")
            rule.onNodeWithText("Salva modifiche").performScrollTo().clicca()
            attendi("Sala modificata correttamente.")
            rule.onNodeWithText("Capienza: 45").performScrollTo().assertIsDisplayed()
            rule.onNodeWithText("Elimina").performScrollTo().clicca()
            rule.onNodeWithText("Annulla").clicca()
            rule.onNodeWithText("Sala aggiornata").assertExists()

            val id = runBlocking { db.salaDao().osservaSale().first().single().id }
            runBlocking { db.corsoDao().insertAll(
                it.univaq.grr.fitslot.data.local.entity.CorsoEntity(
                    name = "Corso collegato", salaAssegnataId = id, fasciaOrariaId = 1)) }
            rule.onNodeWithText("Elimina").performScrollTo().clicca()
            rule.onAllNodesWithText("Elimina").onLast().clicca()
            attendi("Impossibile eliminare la sala: è assegnata a uno o più corsi.")
            rule.onNodeWithText("Sala aggiornata").assertExists()
            runBlocking { db.corsoDao().delete(db.corsoDao().getALl().single()) }
            rule.onNodeWithText("Elimina").performScrollTo().clicca()
            rule.onAllNodesWithText("Elimina").onLast().clicca()
            attendi("Sala eliminata correttamente.")
            rule.onNodeWithText("Sala aggiornata").assertDoesNotExist()
        } finally { runBlocking { db.close() } }
    }

    @Test fun nonGestoreNonVedeLeOperazioniDelleTreUS() {
        impostaUtenteGestioneTest(it.univaq.grr.fitslot.domain.models.Ruolo.ISCRITTO)
        val db = database()
        try {
            avvia(db)
            rule.onNodeWithText("Gestisci sale").assertDoesNotExist()
            rule.onNodeWithText("Gestisci abbonamenti").assertDoesNotExist()
            rule.onNodeWithText("Gestisci piani a punti").assertDoesNotExist()
            rule.onNodeWithText("Go to Login screen").assertExists()
        } finally { runBlocking { db.close() } }
    }

    @Test fun routeGestioneProtettaAncheSeIlRuoloCambiaPrimaDelClick() {
        val db = database()
        try {
            avvia(db)
            // La Home è già visibile: il controllo della destinazione deve essere indipendente.
            impostaUtenteGestioneTest(null)
            rule.onNodeWithText("Gestisci sale").clicca()
            attendi("Accesso riservato al gestore.")
            rule.onNodeWithText("Accedi").assertExists()
            rule.onNodeWithText("Nome").assertDoesNotExist()
        } finally { runBlocking { db.close() } }
    }
}
