package it.univaq.grr.fitslot.domain.exceptions

sealed class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class FileNotFound(path: String, cause: Throwable? = null) : StorageException("File non trovato: $path", cause)
    class InvalidPath(path: String) : StorageException("Percorso non valido o non autorizzato: $path")
    class ReadWriteError(message: String, cause: Throwable? = null) : StorageException(message, cause)
}