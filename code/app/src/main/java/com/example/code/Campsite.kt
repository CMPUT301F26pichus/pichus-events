package com.example.code

import android.graphics.Bitmap
import com.google.firebase.firestore.GeoPoint
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

/**
 * Represents all campsites in the campsite booking app
 *
 * A campsite has a name, location, occupancy, price, rating, image, policy and owner ID.
 *
 * @property name the name of the campsite
 * @property location geolocation of the campsite
 * @property locationName readable location shown on cards and details
 * @property description general campsite information (ex. amenities)
 * @property occupancy max occupancy of the campsite
 * @property price cost per night to book the campsite
 * @property rating rating (1-5) of the campsite
 * @property policy rules set by the owner of the campsite
 * @property ownerID id of the campsite owner
 * @property tags optional hashtags that the campsite owner wants to add
 */
class Campsite(var name: String = "",
               var location: GeoPoint? = null, // Confirmed that this was the suitable data type to use from Google, Gemini, "can the firebase geolocation data type be used to display a point in map view?", 2026-11-14
               var locationName: String = "", // Readable location for display
               var description: String = "",
               var occupancy: Int = -1,
               var price: Double = -1.0,
               var rating: Double = 0.0,
               var policy: String = "",
               var ownerID: Int = -1,
               var tags: String = "") {
    val id: String = ownerID.toString() + "_" + this.name

    /**
     * Creates and returns a promotional QR code bitmap for display
     *
     * @return bitmap for the generated campsite QR code
     */
    fun createQRCode(): Bitmap {
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(this.id, BarcodeFormat.QR_CODE, 350, 350)
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(bitMatrix)
    }
}