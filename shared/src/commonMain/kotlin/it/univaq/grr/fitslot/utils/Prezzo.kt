package it.univaq.grr.fitslot.utils

/** I prezzi sono interi in centesimi, per evitare arrotondamenti. */
fun prezzoInCentesimi(testo: String): Long? {
    val valore = testo.trim().replace(',', '.')
    val formatoPrezzo = Regex("[0-9]+([.][0-9]{1,2})?")
    if (!formatoPrezzo.matches(valore)) return null

    val parti = valore.split('.')
    val euro = parti[0].toLongOrNull() ?: return null
    var centesimi = 0L
    if (parti.size == 2) {
        centesimi = parti[1].padEnd(2, '0').toLong()
    }

    // Non deve essere superato il valore massimo di Long.
    if (euro > (Long.MAX_VALUE - centesimi) / 100) return null
    return euro * 100 + centesimi
}

fun formattaPrezzo(centesimi: Long): String {
    val euro = centesimi / 100
    val resto = (centesimi % 100).toString().padStart(2, '0')
    return "$euro,$resto"
}
