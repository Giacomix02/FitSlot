package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp


@Composable
fun VisualizzazioneCertificatoMedico(
    certificatoImage: ImageBitmap?,
    dataScadenza: String?,
    inScadenza: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(Modifier.heightIn(max = 240.dp), contentAlignment = Alignment.TopCenter) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (certificatoImage == null) {
                Text("Nessuna immagine per questo certificato")
            } else {
                Image(
                    bitmap = certificatoImage,
                    contentDescription = "Certificato medico di esempio, dati mock",
                    modifier = Modifier // follow window width
                        .weight(1f, fill = false)      // height derived from the image, not from pixels
                        .fillMaxWidth(), // cap so short windows don't get blocked
                    contentScale = ContentScale.Fit, // never crop/distort
                )
            }
            if (dataScadenza == null) Text("Nessuna data di scadenza inserita")
            else Text("Data di scadenza: $dataScadenza")
            if (inScadenza) Text("Certificato medico scaduto: $dataScadenza")
        }
    }
}