package com.liulkovich.florapoint.presentation.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.liulkovich.florapoint.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.lang.Math.toRadians
import kotlin.math.cos

@Composable
fun DownloadAreaScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current
    val progress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    var isDownloading by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var regionName by remember { mutableStateOf("") }

    val mapView = remember { MapView(context) }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mapView.controller.animateTo(
                        GeoPoint(location.latitude, location.longitude)
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    LaunchedEffect(progress?.isFinished) {
        if (progress?.isFinished == true) {
            isDownloading = false
        }
    }

    LaunchedEffect(progress?.isError) {
        if (progress?.isError == true) {
            isDownloading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.download_map),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )

        Text(
            text = stringResource(R.string.download_map_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    mapView.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(false)
                        controller.setZoom(13.0)
                        controller.setCenter(GeoPoint(53.133562, 25.141006))
                    }
                }
            )

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .border(
                        width = 2.dp,
                        color = Color(0xFF1B5E20),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            Text(
                text = stringResource(R.string.download_area),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1B5E20),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 230.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                            fusedClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    mapView.controller.animateTo(
                                        GeoPoint(location.latitude, location.longitude)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.LocationSearching, contentDescription = null)
                }
                FloatingActionButton(
                    onClick = { mapView.controller.zoomIn() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
                FloatingActionButton(
                    onClick = { mapView.controller.zoomOut() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
            }
        }

        if (isDownloading) {
            progress?.let { p ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (p.total > 0) {
                        LinearProgressIndicator(
                            progress = { p.current.toFloat() / p.total.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.downloaded_tiles_progress,
                                p.current,
                                p.total
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (progress?.isError == true) {
            Text(
                text = stringResource(R.string.error_area_too_large),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }

        if (progress?.isFinished == true && !isDownloading) {
            Text(
                text = stringResource(R.string.map_saved_success),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1B5E20),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { showNameDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !isDownloading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
        ) {
            Text(
                text = if (isDownloading)
                    stringResource(R.string.loading)
                else
                    stringResource(R.string.download_this_area),
                color = Color.White
            )
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.region_name)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.region_name_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = regionName,
                        onValueChange = { regionName = it },
                        placeholder = { Text(stringResource(R.string.region_name_example)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    val areaInfo = remember(mapView.boundingBox) {
                        try {
                            val bb = mapView.boundingBox
                            val centerLat = (bb.latNorth + bb.latSouth) / 2.0

                            val latDiffKm = (bb.latNorth - bb.latSouth) * 111.32
                            val lonDiffKm = (bb.lonEast - bb.lonWest) * 111.32 * cos(toRadians(centerLat))

                            val areaKm2 = latDiffKm * lonDiffKm
                            val tilesCount = viewModel.countTiles(bb)
                            val sizeMb = tilesCount * 15_000L / (1024.0 * 1024.0)

                            Triple(areaKm2, tilesCount, sizeMb)
                        } catch (e: Exception) {
                            Triple(0.0, 0, 0.0)
                        }
                    }

                    val (areaKm2, tilesCount, sizeMb) = areaInfo

                    Text(
                        text = stringResource(R.string.area_size, String.format(locale, "%.1f", areaKm2)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B5E20)
                    )

                    Text(
                        text = stringResource(
                            R.string.approx_size_format,
                            String.format(locale, "%.1f", sizeMb),
                            tilesCount
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (regionName.isNotBlank()) {
                            showNameDialog = false
                            isDownloading = true
                            viewModel.resetDownloadProgress()

                            val bb = mapView.boundingBox
                            viewModel.downloadArea(context, bb, regionName)
                            regionName = ""
                        }
                    },
                    enabled = regionName.isNotBlank()
                ) {
                    Text(stringResource(R.string.download))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}