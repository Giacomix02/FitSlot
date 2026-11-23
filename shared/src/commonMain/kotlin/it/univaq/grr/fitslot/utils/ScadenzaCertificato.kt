package it.univaq.grr.fitslot.utils

expect fun dataScadenza(valore: String?): String?
expect fun dataOggi(): String
expect fun aggiungiGiorni(data: String, giorni: Int): String
expect fun dataInMillisecondi(data: String): Long
