package it.univaq.grr.fitslot.data.repository

import androidx.sqlite.SQLiteException
import it.univaq.grr.fitslot.data.local.dao.FakeUserDao
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.utils.PasswordSecurity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UserRepositoryImplTest {

    // bisogna usare runTest perchè il codice reale le funzioni sono supended
    // quindi ricrea il contesto di esecuzione asincrono

    @Test
    fun `login returns user with valid credentials`() = runTest {
        val password = "Password123!"
        val user = createUser(password)
        val repository = UserRepositoryImpl(FakeUserDao(mutableListOf(user)))

        val result = repository.userLogin(user.email, password)

        assertEquals(user.id, result?.id)
        assertEquals(user.email, result?.email)
    }

    @Test
    fun `login returns null with wrong password`() = runTest {
        val user = createUser("Password123!")
        val repository = UserRepositoryImpl(FakeUserDao(mutableListOf(user)))

        val result = repository.userLogin(user.email, "WrongPassword!")

        assertNull(result)
    }

    @Test
    fun `login returns null with unknown email`() = runTest {
        val user = createUser("Password123!")
        val repository = UserRepositoryImpl(FakeUserDao(mutableListOf(user)))

        val result = repository.userLogin("test@test.com", "Password123!")

        assertNull(result)
    }

    @Test
    fun `registration returns 0 (user id) on success`() = runTest {
        val user = createUser("Password123!")
        val repository = UserRepositoryImpl(FakeUserDao())

        val result = repository.userRegistration(user = user.toDomain())

        assertEquals("0", result.toString())
    }

    @Test
    fun `registration returns an Exception for duplicate email`() = runTest {
        val user = createUser("Password123!")
        val repository = UserRepositoryImpl(FakeUserDao(mutableListOf(user)))


        assertFailsWith<SQLiteException> {
            repository.userRegistration(user = user.toDomain())
        }
    }

    private fun createUser(password: String): UserEntity {
        val salt = PasswordSecurity.generateSalt()

        return UserEntity(
            id = 0L,
            email = "mario@example.com",
            password = PasswordSecurity.hashPassword(password, salt),
            salt = salt,
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
