package it.univaq.grr.fitslot.utils

// ci sta un po' di doc così almeno sappiamo cosa usare

expect class MediaStorage() {
    /**
     * Salva l'immagine nel percorso predefinito e restituisce il nome/percorso del file salvato
     */
    suspend fun storeImage(data: ByteArray, extension: String = "jpg"): Result<String>

    /**
     * Recupera i byte dell'immagine dato il suo nome o percorso
     */
    suspend fun retrieveImage(path: String): Result<ByteArray>

    /**
     * Sovrascrive un'immagine esistente
     */
    suspend fun modifyImage(path: String, data: ByteArray): Result<Unit>

    /**
     * Elimina l'immagine
     */
    suspend fun deleteImage(path: String): Result<Boolean>
}