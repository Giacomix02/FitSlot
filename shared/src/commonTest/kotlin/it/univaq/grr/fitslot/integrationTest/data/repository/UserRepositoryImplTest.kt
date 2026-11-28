package it.univaq.grr.fitslot.integrationTest.data.repository

import androidx.sqlite.SQLiteException
import it.univaq.grr.fitslot.unitTest.creaDbInMemoria
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UserRepositoryImplTest {

    var database: AppDatabase? = null

    @BeforeTest
    fun creaDbPerTesting(){
        database = creaDbInMemoria()
    }
    @AfterTest
    fun resettaDbPerTesting(){
        database?.close()
    }


    // bisogna usare runTest perchè il codice reale le funzioni sono supended
    // quindi ricrea il contesto di esecuzione asincrono

    @Test
    fun `login returns user with valid credentials`() = runTest {
        val password = "Password123!"
        val user = createUserNew(password)

        database?.let{
            UserRepositoryImpl(it.userDao()).userRegistration(user = user)
            val result = UserRepositoryImpl(it.userDao()).userLogin(user.email, password = user.password)

            assertEquals(1L, result?.id)
            assertEquals(user.email, result?.email)
        }
    }

    @Test
    fun `login returns null with wrong password`() = runTest {
        val password = "Password123!"
        val user = createUserNew(password)

        database?.let{
            UserRepositoryImpl(it.userDao()).userRegistration(user = user)
            val result = UserRepositoryImpl(it.userDao()).userLogin(user.email, password = "passSbagliata")
            assertNull(result)
        }
    }

    @Test
    fun `login returns null with unknown email`() = runTest {
        val password = "Password123!"
        val user = createUserNew(password)

        database?.let{
            UserRepositoryImpl(it.userDao()).userRegistration(user = user)
            val result = UserRepositoryImpl(it.userDao()).userLogin("mailSbagliata", password = user.password)
            assertNull(result)
        }
    }

    @Test
    fun `registration returns 1 (user id) on success`() = runTest {
        val password = "Password123!"
        val user = createUserNew(password)

        database?.let{
            val result = UserRepositoryImpl(it.userDao()).userRegistration(user = user)
            assertEquals("1", result.toString())

        }

    }

    @Test
    fun `registration returns an Exception for duplicate email`() = runTest {
        val password = "Password123!"
        val user = createUserNew(password)

        database?.let{
            UserRepositoryImpl(it.userDao()).userRegistration(user = user)
            assertFailsWith<SQLiteException> {
                UserRepositoryImpl(it.userDao()).userRegistration(user = user)
            }
        }
    }


    private fun createUserNew(password: String): User {

        return User(
            id = 0L,
            email = "mario@example.com",
            password = password,
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
