package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.ui.screens.certificatoMedico.CertificatoMedicoViewModel
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CertificatoMedicoTest {
    @Test
    fun avvisoSoloNeiVentiGiorniPrimaDellaScadenza() {
        val scadenza = LocalDate.of(2026, 12, 8)
        val viewModel = CertificatoMedicoViewModel(dataScadenza = scadenza)
        assertTrue(viewModel.inScadenza(scadenza.minusDays(20)))
        assertFalse(viewModel.inScadenza(scadenza.minusDays(21)))
        assertFalse(viewModel.inScadenza(scadenza.plusDays(1)))
    }

    @Test
    fun nessunAvvisoSeIlCertificatoManca() {
        val scadenza = LocalDate.of(2026, 12, 8)
        val viewModel = CertificatoMedicoViewModel(false, scadenza)
        assertFalse(viewModel.inScadenza(scadenza.minusDays(20)))
    }
}
