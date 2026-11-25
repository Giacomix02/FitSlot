package it.univaq.grr.fitslot.utils

import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.domain.exceptions.KeyAlreadyExistsException
import it.univaq.grr.fitslot.domain.exceptions.KeyNotFoundException
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import org.junit.After
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GestioneUserLocalStorageTest {


    @BeforeTest
    fun tearDown() {
        LocalStorage.deleteAll()
    }

    @Test
    fun `Aggiunta utente`(){
        assertTrue(GestioneUserLocalStorage.aggiungiUserToLocalStorage(getUser()))
    }

    @Test
    fun `Rimozione utente`(){
        GestioneUserLocalStorage.aggiungiUserToLocalStorage(getUser())
        assertTrue(GestioneUserLocalStorage.rimuoviUserFromLocalStorage())
    }


    @Test
    fun `Rimozione utente non esistente`(){
        assertFailsWith<KeyNotFoundException>{
            GestioneUserLocalStorage.rimuoviUserFromLocalStorage()
        }
    }

    @Test
    fun `Aggiunta utente gia aggiunto`(){
        GestioneUserLocalStorage.aggiungiUserToLocalStorage(getUser())

        assertFailsWith<KeyAlreadyExistsException>{
            GestioneUserLocalStorage.aggiungiUserToLocalStorage(getUser())
        }
    }

    @Test
    fun `Utente in memoria ha il ruolo richiesto`(){
        GestioneUserLocalStorage.aggiungiUserToLocalStorage(getUser())

        assertTrue {
            GestioneUserLocalStorage.controllaUserRole(Ruolo.ISCRITTO)
        }
    }

    @Test
    fun `Utente in memoria NON ha il ruolo richiesto`(){
        GestioneUserLocalStorage.aggiungiUserToLocalStorage(getUser())

        assertFalse {
            GestioneUserLocalStorage.controllaUserRole(Ruolo.ALLENATORE)
        }
    }

    @Test
    fun `Si controlla il ruolo ma l'utente non e' stato messo nel Local Storage`(){

        assertFalse{
            GestioneUserLocalStorage.controllaUserRole(Ruolo.ISCRITTO)
        }
    }






    private fun getUser(): User {
        return User(
            id = 1,
            password = "test",
            email = "test",
            username = "test",
            nome = "test",
            cognome = "test",
            dataNascita = "test",
            telefono = "123456789",
            indirizzo = "indirizzo",
            citta = "citta",
            emailContatto = "email",
            ruolo = Ruolo.ISCRITTO
        )
    }

}