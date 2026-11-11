package it.univaq.grr.fitslot.data.local

import it.univaq.grr.fitslot.domain.exceptions.KeyAlreadyExistsException
import it.univaq.grr.fitslot.domain.exceptions.KeyNotFoundException
import it.univaq.grr.fitslot.domain.exceptions.LocalStorageException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class LocalStorageTest {

    @Test
    fun `Retrive chiave già esistente nel local storage`() {
        LocalStorage.addLocalStorage("chiave", "valore")
        assertEquals(LocalStorage.getLocalStorage("chiave"), "valore")
        LocalStorage.deleteAll()
    }

    @Test
    fun `Retrive chiave non esistente nel local storage (solleva eccezione)`() {
        assertFailsWith<KeyNotFoundException> {
            LocalStorage.getLocalStorage("chiave")
        }
    }

    @Test
    fun `Inserimento doppia chiave uguale (solleva eccezione)`() {
        LocalStorage.addLocalStorage("chiave", "valore")
        assertFailsWith<KeyAlreadyExistsException> {
            LocalStorage.addLocalStorage("chiave", "valore")
        }
        LocalStorage.deleteAll()
    }

}