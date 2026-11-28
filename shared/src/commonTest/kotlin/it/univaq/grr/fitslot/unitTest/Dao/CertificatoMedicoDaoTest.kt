package it.univaq.grr.fitslot.unitTest.Dao

import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.unitTest.DatabaseTest
import it.univaq.grr.fitslot.unitTest.scadenzaLontana
import it.univaq.grr.fitslot.unitTest.utente
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class CertificatoMedicoDaoTest: DatabaseTest() {

    val certificatoMedico = CertificatoMedico(1, path = "./test/", scadenza = scadenzaLontana, utente = utente)

    val certificatoMedicoDao get() = database?.certificatoMedicoDao()
    val userDao get() = database?.userDao()

    @Test
    fun insertTest() = runTest {
        userDao?.insertUser(utente.toEntity("testSalt"))

        assertNotEquals(-1, certificatoMedicoDao?.insert(certificatoMedico.toEntity()), "Certificato medico non viene inserito nel database" )
    }

    @Test
    fun getByUserIdTest() = runTest {
        val id = userDao?.insertUser(utente.toEntity("testSalt"))

        certificatoMedicoDao?.insert(certificatoMedico.toEntity())

        val certificatoMedico = certificatoMedicoDao?.getByUserId(id!!)

        assertNotNull(certificatoMedico, "certificato medico non viene associato a utente")
    }

    @Ignore
    @Test
    fun updateTest() = Unit //TODO

    @Ignore
    @Test
    fun getByIdTest() = Unit //TODO

}