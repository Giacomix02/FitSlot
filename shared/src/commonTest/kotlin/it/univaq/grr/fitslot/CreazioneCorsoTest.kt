package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.data.repository.CorsoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.SalaRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.ui.screens.CreazioneCorso.CorsiViewModel
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

val sala = Sala(0, "salaTest", 1)
val fasciaOraria = FasciaOraria(
    0,
    LocalDateTime.parse("2087-10-23T08:00:00"),
    LocalDateTime.parse("2087-10-23T10:00:00")
)

class CreazioneCorsoTest {

    @Test
    fun istanzazioneCorsoTest() {

        assertFailsWith<IllegalArgumentException>("Corso viene creato senza nome") {
            val corsoSenzaNome1 = Corso(0, "", sala, fasciaOraria)
            val corsosenzaNome2 = Corso(0, "    ", sala, fasciaOraria)
        }
        //TODO test del "Sala assegnata è già occupata", ancora da implementare
    }


    @Test
    fun corsoToEntityConversionTest() {

        val corso = Corso(0, "Test", sala, fasciaOraria)
        val corsoEntity = corso.toEntity()

        assertEquals(
            corso.id,
            corsoEntity.id,
            "Conversione corso a entity, id subisce modifiche non attese"
        )
        assertEquals(
            corso.name,
            corsoEntity.name,
            "Conversione corso a entity, nome subisce modifiche non attese"
        )
        assertEquals(
            corso.salaAssegnata.id,
            corsoEntity.salaAssegnataId,
            "Conversione corso a entity, id sala subisce modifiche non attese"
        )
        assertEquals(
            corso.fasciaOraria.id,
            corsoEntity.fasciaOrariaId,
            "Conversione corso a entity, id fascia oraria subisce modifiche non attese"
        )
    }

    class CreazioneCorsoDbTest {


        var database: AppDatabase? = null

        @BeforeTest
        fun creaDbPerTesting() {
            database = creaDbInMemoria()
        }

        @AfterTest
        fun resettaDbPerTesting() {
            database?.close()
        }


//        val corsoRepository: CorsoRepository = CorsoRepositoryImpl(database?.corsoDao())

        @Ignore("Non ancora chiaro come tornare id dall'inserimento")
        @Test
        fun insertInDbTest() {
            val corso = Corso(
                500000,
                "test",
                salaAssegnata = Sala(0, "s", 1),
                FasciaOraria(
                    0,
                    LocalDateTime.parse("2087-10-23T08:00:00"),
                    LocalDateTime.parse("2087-10-23T10:00:00")
                )
            )
//            runTest {assertEquals<Long>(500000, corsoRepository.creaCorso(corso), "Dopo inserimento corso in db, id tornato non atteso") }


        }
        //TODO test salvataggio in db e fetch dal db, controllo di uguaglianza dati

        @Test
        fun CreazioneCorsoVisibilitaFeedbackTest() {
            // Database in memoria: ogni esecuzione parte da uno stato pulito
            // e non usa il database personale dell'utente.
            val viewModel = CorsiViewModel(
                corsoRepository = CorsoRepositoryImpl(database!!.corsoDao()),
                salaRepository = SalaRepositoryImpl(database!!.salaDao())
            )
            val uiState = viewModel.uiState

            runBlocking { viewModel.creaCorso("    ", sala, fasciaOraria) }

            assertNotNull(
                uiState.value.erroreCreazioneCorso,
                "Errore di creazione corso non visibile"
            )
            viewModel.onFieldInteraction()
            assertNull(
                uiState.value.erroreCreazioneCorso,
                "Errore creazione corso non scompare quando succede interazione"
            )
            assertFalse(
                uiState.value.showCreazioneCorsoEseguita,
                "icona creazione corso non scompare quando succede interazione"
            )

            runBlocking{ viewModel.creaCorso("T", sala, fasciaOraria) }

            assertTrue(
                uiState.value.showCreazioneCorsoEseguita,
                "Icona creazione corso non appare quando viene creato il corso"
            )
        }


    }
}