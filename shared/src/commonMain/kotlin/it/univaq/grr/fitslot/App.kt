package it.univaq.grr.fitslot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import it.univaq.grr.fitslot.domain.repository.SalaRepository


/** Passa soltanto le dipendenze delle schermate di Roby alla navigazione del team. */
@Composable
fun App(salaRepository: SalaRepository, catalogoRepository: CatalogoRepository) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Navigation(salaRepository, catalogoRepository) { }
        }
    }
}
