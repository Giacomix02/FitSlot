package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.domain.models.User

fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        email = this.email,
        password = this.password,
        username = this.username,
        nome = this.nome,
        cognome = this.cognome,
        dataNascita = this.dataNascita,
        telefono = this.telefono,
        indirizzo = this.indirizzo,
        citta = this.citta,
        emailContatto = this.emailContatto,
        ruolo = this.ruolo
    )
}

fun User.toEntity(salt: String): UserEntity {
    return UserEntity(
        id = this.id,
        email = this.email,
        password = this.password,
        salt = salt,
        username = this.username,
        nome = this.nome,
        cognome = this.cognome,
        dataNascita = this.dataNascita,
        telefono = this.telefono,
        indirizzo = this.indirizzo,
        citta = this.citta,
        emailContatto = this.emailContatto,
        ruolo = this.ruolo
    )
}