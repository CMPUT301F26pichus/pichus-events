package com.example.code.components

import android.graphics.BitmapFactory
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.code.ImageDB

@Composable
fun CampsiteImageCarousel(
    campsiteId: String,
    imageDB: ImageDB,
    modifier: Modifier = Modifier
) {
    // Retrieve all images associated with this campsite.
    val campsiteImages = imageDB.images.filter { image ->
        image.imageType == "CAMPSITE" &&
                image.campsiteId == campsiteId
    }

    // Don't display a carousel if the campsite has no images.
    if (campsiteImages.isNotEmpty()) {

        val pagerState = rememberPagerState(
            pageCount = { campsiteImages.size }
        )

        Column(modifier = modifier.fillMaxWidth()) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) { page ->

                val image = campsiteImages[page]

                val bitmap = remember(image.id) {
                    image.imageData?.toBytes()?.let { bytes ->
                        BitmapFactory.decodeByteArray(
                            bytes,
                            0,
                            bytes.size
                        )?.asImageBitmap()
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Campsite image ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Image position indicator.
            Text(
                text = "${pagerState.currentPage + 1} / ${campsiteImages.size}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            )
        }
    }
}