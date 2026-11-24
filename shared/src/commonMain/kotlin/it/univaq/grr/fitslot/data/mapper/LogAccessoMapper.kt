package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.LogAccessoEntity
import it.univaq.grr.fitslot.domain.models.LogAccesso
import it.univaq.grr.fitslot.domain.models.User

fun LogAccessoEntity.toDomain(user: User): LogAccesso {
    return LogAccesso(
        id = this.id,
        user = user,
        timestamp = this.timestamp
    )
}

fun LogAccesso.toEntity(): LogAccessoEntity {
    return LogAccessoEntity(
        id = this.id,
        userId = this.user.id,
        timestamp = this.timestamp
    )
}