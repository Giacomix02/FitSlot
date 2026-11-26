package it.univaq.grr.fitslot.unitTest

import it.univaq.grr.fitslot.domain.models.*
import java.time.LocalDateTime


/* se serve modificarle fate .copy() */

val utente = User(3, "testEmail", "testPassword", "testUsername", "testName", "testSurname",   "1/1/1970", "000000000", "tes test, 0", "testCittà", "testEmailContatto", Ruolo.ISCRITTO)

val dataCur = 1795713395000 //2026-11-26
val scadenzaVicina = 1796058995000 //2026-11-30
val scadenzaLontana = 1798650995000 //2026-12-30

val certificatoMedicoScadenzaLunga = CertificatoMedico(1, "./test/", scadenzaLontana, utente)

val fasceOrarie = listOf(
    FasciaOraria(
        0,
        LocalDateTime.parse("2087-10-23T08:00:00"),
        LocalDateTime.parse("2087-10-23T10:00:00")
    ),
    FasciaOraria(
        1,
        LocalDateTime.parse("2087-10-23T14:00:00"),
        LocalDateTime.parse("2087-10-23T16:00:00")
    )
)

val sala = Sala(0, "salaTest", 1)