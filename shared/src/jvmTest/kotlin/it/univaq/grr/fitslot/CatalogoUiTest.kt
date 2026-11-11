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

    private fun avvia(db: AppDatabase) {
        rule.setContent { App(SalaRepositoryImpl(db.salaDao()), CatalogoRepositoryImpl(db.catalogoDao())) }
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

}
