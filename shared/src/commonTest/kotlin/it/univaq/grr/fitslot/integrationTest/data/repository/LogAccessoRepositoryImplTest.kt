package it.univaq.grr.fitslot.integrationTest.data.repository

import androidx.sqlite.SQLiteException
import it.univaq.grr.fitslot.unitTest.creaDbInMemoria
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.LogAccessoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.domain.models.LogAccesso
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LogAccessoRepositoryImplTest {

    var database: AppDatabase? = null

    @BeforeTest
    fun creaDbPerTesting(){
        database = creaDbInMemoria()
    }
    @AfterTest
    fun resettaDbPerTesting(){
        database?.close()
    }


    @Test
    fun `log dell'accesso effettuato con successo`() = runTest {

        val user = createUserNew()

        val accesso = LogAccesso(
            id = 0L,
            user = user,
            timestamp = System.currentTimeMillis()
        )

        database?.let{
            UserRepositoryImpl(it.userDao()).userRegistration(user = user)

            val result = LogAccessoRepositoryImpl(it.logAccessoDao()).insertAccesso(accesso)

            assertEquals(1L, result)
        }
    }

    @Test
    fun `La repository non riceve uno user non esistente`() = runTest {

        val user = createUserNew()

        val accesso = LogAccesso(
            id = 0L,
            user = User(
                id = 3L,
                email = "mario@example.com",
                password = "testPass",
                username = "mario",
                nome = "Mario",
                cognome = "Rossi",
                dataNascita = "01/01/2000",
                telefono = "3331234567",
                indirizzo = "Via Roma 1",
                citta = "L'Aquila",
                emailContatto = "mario@example.com",
                ruolo = Ruolo.ISCRITTO
            ),
            timestamp = System.currentTimeMillis()
        )

        database?.let{
            UserRepositoryImpl(it.userDao()).userRegistration(user = user)

            assertFailsWith<SQLiteException>{
                LogAccessoRepositoryImpl(it.logAccessoDao()).insertAccesso(accesso)
            }

        }
    }


    private fun createUserNew(): User {

        return User(
            id = 1L,
            email = "mario@example.com",
            password = "testPass",
            username = "mario",
            nome = "Mario",
            cognome = "Rossi",
            dataNascita = "01/01/2000",
            telefono = "3331234567",
            indirizzo = "Via Roma 1",
            citta = "L'Aquila",
            emailContatto = "mario@example.com",
            ruolo = Ruolo.ISCRITTO
        )
    }



}