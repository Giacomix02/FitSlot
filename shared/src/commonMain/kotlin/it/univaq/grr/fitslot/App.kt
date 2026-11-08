package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.repository.*

/** Applica il tema e passa le dipendenze condivise alla navigazione. */
@Composable
fun App(salaRepository: SalaRepository, userRepository: UserRepository,
        catalogoRepository: CatalogoRepository, sessione: SessioneUtente) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Navigation(salaRepository, userRepository, catalogoRepository, sessione)
        }
    }
}
