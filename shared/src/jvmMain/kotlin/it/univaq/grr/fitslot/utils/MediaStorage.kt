package it.univaq.grr.fitslot.utils

import it.univaq.grr.fitslot.domain.exceptions.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

actual class MediaStorage actual constructor() {

    private val defaultDir: Path = Paths.get(System.getProperty("user.home"), ".fitslot", "media")

    init {
        if (!defaultDir.exists()) {
            defaultDir.createDirectories()
        }
    }

    actual suspend fun storeImage(data: ByteArray, extension: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanExt = extension.trimStart('.')
            val fileName = "${UUID.randomUUID()}.$cleanExt"
            val destination = defaultDir.resolve(fileName).normalize()

            if (!destination.startsWith(defaultDir)) {
                return@withContext Result.failure(StorageException.InvalidPath(fileName))
            }

            destination.writeBytes(data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            Result.success(fileName)
        } catch (e: IOException) {
            Result.failure(StorageException.ReadWriteError("Errore durante il salvataggio dell'immagine", e))
        } catch (e: Exception) {
            Result.failure(StorageException.ReadWriteError("Errore imprevisto: ${e.message}", e))
        }
    }

    actual suspend fun retrieveImage(path: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val targetFile = resolveSafePath(path)

            if (!targetFile.exists()) {
                return@withContext Result.failure(StorageException.FileNotFound(path))
            }

            Result.success(targetFile.readBytes())
        } catch (e: StorageException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(StorageException.ReadWriteError("Impossibile leggere il file: $path", e))
        }
    }

    actual suspend fun modifyImage(path: String, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val targetFile = resolveSafePath(path)

            if (!targetFile.exists()) {
                return@withContext Result.failure(StorageException.FileNotFound(path))
            }

            targetFile.writeBytes(data, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
            Result.success(Unit)
        } catch (e: StorageException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(StorageException.ReadWriteError("Errore durante la modifica di: $path", e))
        }
    }

    actual suspend fun deleteImage(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val targetFile = resolveSafePath(path)
            Result.success(targetFile.deleteIfExists())
        } catch (e: StorageException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(StorageException.ReadWriteError("Errore durante l'eliminazione di: $path", e))
        }
    }

    private fun resolveSafePath(path: String): Path {
        val resolved = defaultDir.resolve(Paths.get(path).fileName).normalize()
        if (!resolved.startsWith(defaultDir)) {
            throw StorageException.InvalidPath(path)
        }
        return resolved
    }
}