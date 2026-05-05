package com.liulkovich.florapoint.presentation.components

import android.R.attr.category
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import com.liulkovich.florapoint.presentation.screens.map.utils.createShapeMarkerBitmap
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    points: List<UserPoints>,
    species: List<Reference>,
    selectedPointId: Int? = null,
    currentLocation: Pair<Double, Double>?,
    onMapReady: (MapView, MyLocationNewOverlay) -> Unit,
    onMarkerClick: (UserPoints) -> Unit,
    onMarkerLongClick: (UserPoints) -> Unit,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var isInitialized by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        factory = {
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setCenter(GeoPoint(53.133562, 25.141006))
                controller.setZoom(13.0)

                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()
                overlays.add(locationOverlay)

                onMapReady(this, locationOverlay)
            }
        },
        update = { mv ->
            if (!isInitialized && currentLocation != null) {
                mv.controller.setCenter(GeoPoint(currentLocation.first, currentLocation.second))
                isInitialized = true
            }

            // Удаляем старые маркеры
            mv.overlays.removeAll { it is Marker || it is MapEventsOverlay }

            // Добавляем маркеры
            points.forEach { point ->
                val ref = species.find { it.id == point.speciesId }

                // Отладка
                println("DEBUG POINT: id=${point.id}, speciesId=${point.speciesId}, savedCategory=${point.category}, final=$category")

                val category = when {
                    !point.category.isNullOrBlank() -> point.category!!.lowercase().trim()

                    ref != null && !ref.category.isNullOrBlank() -> ref.category!!.lowercase().trim()

                    else -> "custom"
                }

                val bitmap = createShapeMarkerBitmap(category)

                val marker = Marker(mv).apply {
                    position = GeoPoint(point.latitude, point.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    title = point.userName.ifBlank { ref?.name ?: "Точка ${point.id}" }
                    snippet = ref?.name

                    icon = BitmapDrawable(context.resources, bitmap)

                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick(point)
                        true
                    }
                }

                mv.overlays.add(marker)
            }

            mv.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                override fun longPressHelper(p: GeoPoint?): Boolean = false
            }))

            mv.invalidate()
        }
    )
}
