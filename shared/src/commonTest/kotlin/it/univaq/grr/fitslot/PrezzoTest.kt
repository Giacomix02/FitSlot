package it.univaq.grr.fitslot

import it.univaq.grr.fitslot.utils.formattaPrezzo
import it.univaq.grr.fitslot.utils.prezzoInCentesimi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PrezzoTest {
    @Test
    fun conversioneDelPrezzo() {
        assertEquals(1234L, prezzoInCentesimi("12,34"))
        assertEquals("12,34", formattaPrezzo(1234))
        assertNull(prezzoInCentesimi("ciao"))
    }
}
