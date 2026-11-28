package it.univaq.grr.fitslot.integrationTest.data.repository

import androidx.sqlite.SQLiteException
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.CertificatoMedicoRepositoryImpl
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.unitTest.creaDbInMemoria
import it.univaq.grr.fitslot.unitTest.scadenzaLontana
import it.univaq.grr.fitslot.unitTest.utente
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class CertificatoMedicoRepositoryImplTest {

    private var database: AppDatabase? = null

    val imageStream : ByteArray = this::class.java.getResourceAsStream("/CertificatoMedicoExample.png")!!.readBytes()

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
                    scadenza = scadenzaLontana,
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
                        scadenza = scadenzaLontana,
                        utente = getUser()
                    ))
            }
        }
    }

    @Test
    fun `get certificato medico da user`() = runTest {
        database?.let { database ->

            val certificatoMedicoRepository = CertificatoMedicoRepositoryImpl(database.certificatoMedicoDao())

            val userRepository = UserRepositoryImpl(database.userDao())

            val registeredUserId = userRepository.userRegistration(utente)
            val registeredUser = utente.copy(id = registeredUserId)

            certificatoMedicoRepository.addCertificatoMedico(
                CertificatoMedico(
                    id = 0,
                    path = "./test/",
                    scadenza = scadenzaLontana,
                    utente = registeredUser
                )
            )

            assertNotNull(certificatoMedicoRepository
                .getCertificatoMedicoByUser(registeredUser)
            )
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