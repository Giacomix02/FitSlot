package it.univaq.grr.fitslot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform