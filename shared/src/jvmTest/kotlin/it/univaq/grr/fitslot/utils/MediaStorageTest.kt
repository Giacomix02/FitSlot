package it.univaq.grr.fitslot.utils

import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MediaStorageTest {
    val path = "./test/"
    val mediaStorage = MediaStorage()
    val imageStream : ByteArray = this::class.java.getResourceAsStream("/CertificatoMedicoExample.png")!!.readBytes()

    @AfterTest
    fun deleteLocalImageAfterTest() = Unit //TODO


    @Test
    fun storeAndRetrieveImageTest() = runTest{

        val result = mediaStorage.storeImage(imageStream, "png")

        assertTrue(result.isSuccess)
        assertNotNull(mediaStorage.retrieveImage(path))

    }
}