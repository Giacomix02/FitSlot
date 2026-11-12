package it.univaq.grr.fitslot.ui.screens.creazioneSala
import it.univaq.grr.fitslot.domain.models.Sala
data class CreazioneSalaUiState(
    val nome: String = "", val capienza: String = "",
    val elenco: List<Sala> = emptyList(), val caricamento: Boolean = true,
    val idModifica: Long? = null, val salvataggioInCorso: Boolean = false,
    val errore: String? = null, val conferma: String? = null, val daEliminare: Sala? = null
)
