package it.univaq.grr.fitslot.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun immagineCertificato(bytes: ByteArray): ImageBitmap? =
    try { Image.makeFromEncoded(bytes).toComposeImageBitmap() } catch (e: Exception) { null }
