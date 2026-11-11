package it.univaq.grr.fitslot.utils

import io.github.cdimascio.dotenv.dotenv
import java.io.File


actual object EnvRetriever {
    private val dotenv by lazy {
        // Risali dalla working directory fino a trovare la cartella che contiene .env
        val projectRoot = findProjectRoot()
        dotenv {
            ignoreIfMissing = true
            if (projectRoot != null) {
                directory = projectRoot.absolutePath
            }
        }
    }

    /**
     * Cerca il file .env risalendo dall'attuale working directory verso l'alto.
     * Questo è necessario perché Gradle/IntelliJ possono avviare il processo
     * da desktopApp/ o da build/, non dalla root del progetto.
     */
    private fun findProjectRoot(): File? {
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            if (File(dir, ".env").exists()) return dir
            dir = dir.parentFile
        }
        return null
    }

    actual fun get(key: String): String? {
        return dotenv[key] ?: System.getenv(key)
    }
}