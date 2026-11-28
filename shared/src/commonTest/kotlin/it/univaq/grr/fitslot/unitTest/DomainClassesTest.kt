package it.univaq.grr.fitslot.unitTest


import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class DomainClassesTest {


    @Test
    fun istanzazioneCorsoTest() {

        assertFailsWith<IllegalArgumentException>("Corso viene creato senza nome") {
            val corsoSenzaNome1 = Corso(0, "", sala, fasceOrarie[0])
            val corsosenzaNome2 = Corso(0, "    ", sala, fasceOrarie[0])
        }
    }

    @Test
    fun corsoToEntityConversionTest() {

        val corso = Corso(0, "Test", sala, fasceOrarie[0])
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

    @Test
    fun istanzazioneFasciaOrariaTest() {
        assertFailsWith<IllegalArgumentException>("FasciaOraria viene creata con durata negativa") {
            val FasciaConDurataNegativa = FasciaOraria(
                0,
                LocalDateTime.parse("2087-10-23T10:00:00"),
                LocalDateTime.parse("2087-10-23T08:00:00")
            )
        }
        assertFailsWith<IllegalArgumentException>("FasciaOraria viene creata con durata maggiore del massimo") {
            val FasciaConDurataPiuDiMax = FasciaOraria(
                0,
                LocalDateTime.parse("2087-10-23T10:00:00"),
                LocalDateTime.parse("2087-10-23T10:00:00").plusHours(FasciaOraria.DURATA_MASSIMA_ORE+1L)
            )
        }
    }

    @Test
    fun fasciaOrariaToEntityConversion() {
        val fasciaOrariaEntity = fasceOrarie[0].toEntity()

        assertEquals(
            fasceOrarie[0].id, fasciaOrariaEntity.id, "Conversione FasciaOraria a entity, id subisce modifiche non attese"
        )
        assertEquals(
            fasceOrarie[0].orarioInizio.toString(), fasciaOrariaEntity.orarioInizio, "Conversione FasciaOraria a entity, orarioInizio subisce modifiche non attese"
        )
        assertEquals(
            fasceOrarie[0].orarioFine.toString(), fasciaOrariaEntity.orarioFine, "Conversione FasciaOraria a entity, orarioFine subisce modifiche non attese"
        )

    }

}