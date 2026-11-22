package it.univaq.grr.fitslot.ui.screens.certificatoMedico

import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CertificatoMedicoViewModel(
    // Dati mock previsti dalla GRR-43, in attesa del collegamento ai certificati.
    val certificatoPresente: Boolean = true,
    val dataScadenza: LocalDate = LocalDate.now().plusDays(20)
) : ViewModel() {
    fun inScadenza(oggi: LocalDate = LocalDate.now()): Boolean {
        val giorniMancanti = ChronoUnit.DAYS.between(oggi, dataScadenza)
        return certificatoPresente && giorniMancanti >= 0 && giorniMancanti <= 20
    }
}
