package it.univaq.grr.fitslot.data.repository

import androidx.sqlite.SQLiteException
import it.univaq.grr.fitslot.creaDbInMemoria
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import kotlinx.coroutines.test.runTest
import java.sql.SQLException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CertificatoMedicoRepositoryImplTest {

    private var database: AppDatabase? = null

    @BeforeTest
    fun creaDbPerTesting(){
        database = creaDbInMemoria()
    }
    @AfterTest
    fun resettaDbPerTesting(){
        database?.close()
    }

    @Test
    fun `aggiunta certificato medico`() = runTest {

        database?.let{
            UserRepositoryImpl(it.userDao()).userRegistration(
                user = getUser()
            )
        }

        database?.let {
            val result = CertificatoMedicoRepositoryImpl(it.certificatoMedicoDao())
                .addCertificatoMedico(CertificatoMedico(
                    id = 0,
                    path = "path",
                    scadenza = "scadenza",
                    utente = getUser()
                ))
            assertEquals(1L, result)
        }
    }


    @Test
    fun `aggiunta certificato medico con utente inesistente`() = runTest {


        database?.let {

            assertFailsWith<SQLiteException> {
                CertificatoMedicoRepositoryImpl(it.certificatoMedicoDao())
                    .addCertificatoMedico(CertificatoMedico(
                        id = 0,
                        path = "path",
                        scadenza = "scadenza",
                        utente = getUser()
                    ))
            }
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