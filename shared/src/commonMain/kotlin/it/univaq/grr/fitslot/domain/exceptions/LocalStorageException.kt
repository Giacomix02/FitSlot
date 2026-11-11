package it.univaq.grr.fitslot.domain.exceptions

sealed class LocalStorageException(message: String) : Exception(message)

class KeyNotFoundException(key: String) :
    LocalStorageException("La chiave '$key' non esiste nello storage.")

class KeyAlreadyExistsException(key: String) :
    LocalStorageException("La chiave '$key' è già presente nello storage.")