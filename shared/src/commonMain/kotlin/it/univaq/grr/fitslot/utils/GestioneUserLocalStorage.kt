package it.univaq.grr.fitslot.utils

import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.domain.exceptions.KeyAlreadyExistsException
import it.univaq.grr.fitslot.domain.exceptions.KeyNotFoundException
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User

object GestioneUserLocalStorage{

    fun aggiungiUserToLocalStorage(user: User): Boolean {
        try {
            LocalStorage.addLocalStorage("user", user)
            return true
        } catch (e: KeyAlreadyExistsException) {
            throw e
        }
    }

    fun rimuoviUserFromLocalStorage(): Boolean {
        try {
            LocalStorage.deleteFromKey("user")
            return true
        } catch (e: KeyNotFoundException) {
            throw e
        }
    }

    fun controllaUserRole(ruolo: Ruolo): Boolean {
        try {
            val user = LocalStorage.getLocalStorage("user") as User
            return user.ruolo == ruolo
        } catch (e: KeyNotFoundException) {
            return false
        }
    }

    fun getCurrentUser(): User? = try {
        LocalStorage.getLocalStorage("user") as? User
    } catch (e: KeyNotFoundException) {
        null
    }

    fun isLoggedIn(): Boolean = getCurrentUser() != null

    fun logout(): Boolean = try {
        LocalStorage.deleteFromKey("user")
        return true
    } catch (e: KeyNotFoundException) {
        return false
    }


}