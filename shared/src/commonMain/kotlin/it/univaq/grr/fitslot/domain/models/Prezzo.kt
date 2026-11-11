package it.univaq.grr.fitslot.domain.models

/** Converte euro in centesimi senza utilizzare numeri a virgola mobile. */
fun prezzoInCentesimi(testo: String): Long? {
    val valore = testo.trim()
    if (!Regex("[0-9]+([.,][0-9]{1,2})?").matches(valore)) return null
    val parti = valore.replace(',', '.').split('.')
    val euro = parti[0].toLongOrNull() ?: return null
    val centesimi = parti.getOrElse(1) { "" }.padEnd(2, '0').toLong()
    if (euro > (Long.MAX_VALUE - centesimi) / 100) return null
    return euro * 100 + centesimi
}

fun formattaPrezzo(centesimi: Long): String =
    "${centesimi / 100},${(centesimi % 100).toString().padStart(2, '0')}"
