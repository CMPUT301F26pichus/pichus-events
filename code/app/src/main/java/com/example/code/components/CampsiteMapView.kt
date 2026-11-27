package com.example.code.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Open-source map viewer component using osmdroid displaying a marker at the specified latitude and longitude.
 */
@Composable
fun CampsiteMapView(
    latitude: Double,
    longitude: Double,
    title: String = "Campsite Location",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Set User-Agent to satisfy OpenStreetMap tile usage policy and prevent "Access blocked" 403 errors
    Configuration.getInstance().userAgentValue =
        "CMPUT301-PichusEvents/1.0"

    Configuration.getInstance().isDebugMode = true
    Configuration.getInstance().isDebugMapTileDownloader = true

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(
                    XYTileSource(
                        "OpenStreetMap",
                        0,
                        20,
                        650,
                        ".png",
                        arrayOf("https://tile.openstreetmap.org/")
                    )
                )
                setMultiTouchControls(true)
                setTilesScaledToDpi(false)
                controller.setZoom(14.0)
                val geoPoint = GeoPoint(latitude, longitude)
                controller.setCenter(geoPoint)

                val marker = Marker(this).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    this.title = title
                }
                overlays.add(marker)
            }
        },
        update = { mapView ->
            val geoPoint = GeoPoint(latitude, longitude)
            mapView.controller.setCenter(geoPoint)
            mapView.overlays.clear()
            val marker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                this.title = title
            }
            mapView.overlays.add(marker)
            mapView.invalidate()
        }
    )
}
