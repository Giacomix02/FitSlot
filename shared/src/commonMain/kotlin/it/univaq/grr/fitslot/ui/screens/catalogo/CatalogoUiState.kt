package it.univaq.grr.fitslot.ui.screens.catalogo

enum class TipoCatalogo { ABBONAMENTI, PIANI_A_PUNTI }

/** La quantità indica giorni per gli abbonamenti e ingressi per i piani a punti. */
data class VoceCatalogo(val id: Long, val nome: String, val prezzoCentesimi: Long, val quantita: Int)

data class CatalogoUiState(
    val elenco: List<VoceCatalogo> = emptyList(),
    val caricamento: Boolean = true,
    val idModifica: Long? = null,
    val nome: String = "",
    val prezzo: String = "",
    val quantita: String = "",
    val operazioneInCorso: Boolean = false,
    val errore: String? = null,
    val conferma: String? = null,
    val daEliminare: VoceCatalogo? = null
)
