package it.univaq.grr.fitslot.utils

expect object EnvRetriever {
    fun get(key: String): String?
}