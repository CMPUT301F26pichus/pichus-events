// Instrumented tests for ImageDB against Firestore. Run on the emulator.
// Uses a fake image ID and deletes every image it adds.

package com.example.code

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageDBTest {
    private val imageDB = ImageDB()
    private val imageID = fakeID().toString()
    private val userID = fakeID()
    private val campsiteID = userID.toString() + "_" + "testSite"

    private fun findTestImage(): Image? = imageDB.images.find { it.id == imageID }

    private fun addTestImage(): Image {
        imageDB.addImage(Image(imageID, imageType = "Test Image",
            userId = userID.toString(), campsiteId = campsiteID))
        waitUntil { findTestImage() != null }
        val image = findTestImage()
        assertNotNull("test image never showed up", image)
        return image!!
    }

    @After
    fun cleanUp() {
        imageDB.deleteImage(imageID)
    }

    @Test
    fun addImageShowsInImages() {
        val image = addTestImage()
        assertEquals(imageID, image.id)
        assertEquals("Test Image", image.imageType)
        assertEquals(userID.toString(), image.userId)
        assertEquals(campsiteID, image.campsiteId)
    }

    @Test
    fun deleteImageRemovesImage() {
        val image = addTestImage()
        imageDB.deleteImage(imageID)
        waitUntil { findTestImage() == null }
        assertTrue(findTestImage() == null)
    }
}
