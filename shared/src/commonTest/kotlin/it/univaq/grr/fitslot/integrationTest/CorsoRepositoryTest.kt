package it.univaq.grr.fitslot.integrationTest

import it.univaq.grr.fitslot.creaDbInMemoria
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala
import java.time.LocalDateTime
import kotlin.test.Ignore
import kotlin.test.Test

open class CorsoRepositoryTest {

    val database = creaDbInMemoria()




    class corsoRepositoryTest {


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
    }
}