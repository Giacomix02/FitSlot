package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.CertificatoMedicoEntity
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.data.repository.UserRepositoryImpl
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.domain.repository.UserRepository

fun CertificatoMedico.toEntity(): CertificatoMedicoEntity{
    return CertificatoMedicoEntity(
        id = this.id,
        userId = this.utente.id,
        path = this.path,
        scadenza = this.scadenza,
    )
}

fun CertificatoMedicoEntity.toDomain(user: User): CertificatoMedico{
    return CertificatoMedico(
        id = this.id,
        utente = user,
        path = this.path,
        scadenza = this.scadenza,
    )
}