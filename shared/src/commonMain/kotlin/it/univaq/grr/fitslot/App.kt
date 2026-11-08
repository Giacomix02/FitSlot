package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.univaq.grr.fitslot.domain.repository.SalaRepository
import it.univaq.grr.fitslot.domain.repository.UserRepository

/**
 * Applica il tema e avvia la navigazione dell'applicazione.
 */
@Composable
fun App(salaRepository: SalaRepository, userRepository: UserRepository) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Navigation(salaRepository, userRepository)
        }
    }
}
