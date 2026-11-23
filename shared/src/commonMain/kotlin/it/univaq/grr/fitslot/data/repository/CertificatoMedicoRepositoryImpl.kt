package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.CertificatoMedicoDAO
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.utils.dataScadenza
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository

class CertificatoMedicoRepositoryImpl(
    private val certificatoMedicoDAO: CertificatoMedicoDAO
) : CertificatoMedicoRepository {

    override suspend fun getCertificatoMedicoByUser(user: User): CertificatoMedico? =
        certificatoMedicoDAO.getByUserId(user.id)?.toDomain(user)

    override suspend fun addCertificatoMedico(certificato: CertificatoMedico): Long =
        certificatoMedicoDAO.insert(certificato.toEntity())

    override suspend fun modifyCertificatoMedico(certificato: CertificatoMedico) {
        TODO("Not yet implemented")
    }
    override suspend fun iscritti(): List<User> {
        PermessiGestioneDaLocalStorage.richiediRuolo(Ruolo.RECEPIONIST)
        return certificatoMedicoDAO.iscritti().map { it.toDomain() }
    }

    override suspend fun certificati(scaduti: Boolean?, dal: String, al: String, oggi: String): List<CertificatoMedico> {
        val risultato = mutableListOf<CertificatoMedico>()
        for (utente in iscritti()) {
            val certificato = getCertificatoMedicoByUser(utente) ?: continue
            if (scaduti == null) {
                risultato.add(certificato)
                continue
            }
            val data = dataScadenza(certificato.scadenza) ?: continue
            if (scaduti && data < oggi) risultato.add(certificato)
            else if (!scaduti && data >= oggi && data >= dal && data <= al) risultato.add(certificato)
        }
        return risultato
    }
}