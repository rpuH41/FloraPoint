package com.liulkovich.florapoint.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import com.liulkovich.florapoint.presentation.screens.map.utils.createShapeMarkerBitmap
import com.liulkovich.florapoint.presentation.screens.map.utils.isOnline
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
    shouldFollowLocation: Boolean = true,
    forceCenter: GeoPoint? = null,
    onMapReady: (MapView, MyLocationNewOverlay) -> Unit,
    onMarkerClick: (UserPoints) -> Unit,
    onMarkerLongClick: (UserPoints) -> Unit,
) {
    val context = LocalContext.current
    val pointTitleTemplate = stringResource(R.string.point_id)
    val mapView = remember { MapView(context) }
    val online by isOnline()

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }

    var isInitialized by remember { mutableStateOf(false) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    controller.setCenter(GeoPoint(53.133562, 25.141006))
                    controller.setZoom(13.0)
                    val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    overlay.enableMyLocation()
                    if (shouldFollowLocation) overlay.enableFollowLocation()
                    overlays.add(overlay)
                    locationOverlay = overlay

                    onMapReady(this, overlay)
                }
            },
            update = { mv ->
                val overlay = locationOverlay ?: return@AndroidView

                if (shouldFollowLocation && !overlay.isFollowLocationEnabled) {
                    overlay.enableFollowLocation()
                } else if (!shouldFollowLocation && overlay.isFollowLocationEnabled) {
                    overlay.disableFollowLocation()
                }

                if (forceCenter != null) {
                    mv.controller.animateTo(forceCenter)
                    if (overlay.isFollowLocationEnabled) {
                        overlay.disableFollowLocation()
                    }
                } else if (!isInitialized && currentLocation != null && shouldFollowLocation) {
                    mv.controller.setCenter(
                        GeoPoint(currentLocation.first, currentLocation.second)
                    )
                    isInitialized = true
                }

                mv.overlays.removeAll { it is Marker || it is MapEventsOverlay }

                points.forEach { point ->
                    val ref = species.find { it.id == point.speciesId }
                    val category = when {
                        !point.category.isNullOrBlank() -> point.category.lowercase().trim()
                        ref != null && ref.category.isNotBlank() -> ref.category.lowercase().trim()
                        else -> "custom"
                    }

                    val bitmap = createShapeMarkerBitmap(category)
                    val marker = Marker(mv).apply {
                        position = GeoPoint(point.latitude, point.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = point.userName.ifBlank {
                            ref?.name ?: pointTitleTemplate.format(point.id)
                        }
                        snippet = ref?.name
                        icon = bitmap.toDrawable(context.resources)
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

        if (!online) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xCC000000)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.offline),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}