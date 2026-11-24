package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.ui.screens.certificatoMedico.certificatoInScadenza
import it.univaq.grr.fitslot.utils.aggiungiGiorni
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CertificatoMedicoTest {
    @Test
    fun avvisoSoloNeiVentiGiorniPrimaDellaScadenza() {
        val scadenza = "2026-12-08"
        assertTrue(certificatoInScadenza(scadenza, aggiungiGiorni(scadenza, -20)))
        assertFalse(certificatoInScadenza(scadenza, aggiungiGiorni(scadenza, -21)))
        assertFalse(certificatoInScadenza(scadenza, aggiungiGiorni(scadenza, 1)))
    }

    @Test
    fun nessunAvvisoSeIlCertificatoManca() {
        val scadenza = "2026-12-08"
        assertFalse(certificatoInScadenza(null, aggiungiGiorni(scadenza, -20)))
    }
}
