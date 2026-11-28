package it.univaq.grr.fitslot.unitTest.utils

import it.univaq.grr.fitslot.ui.screens.FasceOrarieScreen.formatter
import it.univaq.grr.fitslot.utils.parseCatching
import it.univaq.grr.fitslot.utils.toImageBitmap
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ComposableScopeUtilsTest {
    @Test
    fun parseCatchingTest() {
        val invalidDate = "32/11/2026"
        val validDate = "21/11/2026"
        assertNull(parseCatching(invalidDate, formatter))
        assertNotNull(parseCatching(validDate, formatter))
    }

    @Test
    fun conversionImageToBitMapTest() {
        val stream = this::class.java.getResourceAsStream("/CertificatoMedicoExample.png")
        assertNotNull(stream)
        val bytes = stream!!.readBytes()

        val bitmap = bytes.toImageBitmap()
        assertNotNull(bitmap)
    }

}