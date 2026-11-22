package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.ui.screens.certificatoMedico.certificatoInScadenza
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CertificatoMedicoTest {
    @Test
    fun avvisoSoloNeiVentiGiorniPrimaDellaScadenza() {
        val scadenza = LocalDate.of(2026, 12, 8)
        assertTrue(certificatoInScadenza(scadenza, scadenza.minusDays(20)))
        assertFalse(certificatoInScadenza(scadenza, scadenza.minusDays(21)))
        assertFalse(certificatoInScadenza(scadenza, scadenza.plusDays(1)))
    }

    @Test
    fun nessunAvvisoSeIlCertificatoManca() {
        val scadenza = LocalDate.of(2026, 12, 8)
        assertFalse(certificatoInScadenza(null, scadenza.minusDays(20)))
    }
}
