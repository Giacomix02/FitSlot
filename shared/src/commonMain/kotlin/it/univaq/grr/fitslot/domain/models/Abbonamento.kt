package it.univaq.grr.fitslot.domain.models

/** Il piano di abbonamento consente ingressi illimitati per la durata prevista. */
data class Abbonamento(
    val id: Long = 0,
    val nome: String,
    val prezzoCentesimi: Long,
    val durataGiorni: Int
)
