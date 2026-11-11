package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.univaq.grr.fitslot.data.local.AppDatabase
import it.univaq.grr.fitslot.data.repository.SalaRepositoryImpl

/**
 * Applica il tema e avvia la navigazione dell'applicazione.
 */
@Composable
fun App() {
    val database = AppDatabase.create()
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Navigation(salaRepository = SalaRepositoryImpl(
                salaDao = database.salaDao()
            ))
        }
    }
}