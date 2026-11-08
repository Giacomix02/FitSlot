package it.univaq.grr.fitslot.domain.models

/** I punti del piano corrispondono agli ingressi inclusi nel pacchetto. */
data class PianoAPunti(
    val id: Long = 0,
    val nome: String,
    val prezzoCentesimi: Long,
    val ingressiInclusi: Int
)
