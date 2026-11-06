package it.univaq.grr.fitslot.ui.screens.creazionesala

/** Contiene i valori dei campi e lo stato del salvataggio
 * mostrati nella schermata di creazione di una sala.*/

data class CreazioneSalaUiState(
    val nome: String = "",
    val capienza: String = "",
    val salvataggioInCorso: Boolean = false,
    val errore: String? = null,
    val salaCreata: Boolean = false
)