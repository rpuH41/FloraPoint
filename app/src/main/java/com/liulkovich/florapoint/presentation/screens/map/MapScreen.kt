package com.liulkovich.florapoint.presentation.screens.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.presentation.components.AddPointSheetContent
import com.liulkovich.florapoint.presentation.components.EditPointSheetContent
import com.liulkovich.florapoint.presentation.components.OsmMapView
import com.liulkovich.florapoint.presentation.components.PointListItem
import kotlinx.coroutines.flow.collectLatest
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    deepLinkLat: Double? = null,
    deepLinkLon: Double? = null,
    deepLinkName: String? = null,
    deepLinkCategory: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val myLocationOverlayRef = remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var shouldFollowLocation by remember { mutableStateOf(true) }
    var forceCenter by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(deepLinkLat, deepLinkLon) {
        if (deepLinkLat != null && deepLinkLon != null && deepLinkLat != 0.0) {
            shouldFollowLocation = false
            forceCenter = GeoPoint(deepLinkLat, deepLinkLon)
            viewModel.onAddNewPointClicked(deepLinkLat, deepLinkLon)
            deepLinkName?.let { viewModel.setDeepLinkData(it, deepLinkCategory ?: "custom") }
        }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateCurrentLocation(location.latitude, location.longitude)
                }
            }
            myLocationOverlayRef.value?.enableMyLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateCurrentLocation(location.latitude, location.longitude)
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.command.collectLatest { command ->
            when (command) {
                is MapCommand.CenterMapOnPoint -> {
                    shouldFollowLocation = false
                    forceCenter = GeoPoint(command.point.latitude, command.point.longitude)
                }
                else -> Unit
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    points = state.userPoints,
                    species = state.species,
                    selectedPointId = state.selectedPointId,
                    currentLocation = state.currentUserLocation,
                    shouldFollowLocation = shouldFollowLocation,
                    forceCenter = forceCenter,
                    onMapReady = { mapView, locationOverlay ->
                        mapViewRef.value = mapView
                        myLocationOverlayRef.value = locationOverlay
                    },
                    onMarkerClick = { point ->
                        viewModel.onPointClicked(point)
                        shouldFollowLocation = false
                        forceCenter = GeoPoint(point.latitude, point.longitude)
                    },
                    onMarkerLongClick = { point -> viewModel.onPointLongClicked(point) }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(onClick = {
                        shouldFollowLocation = true
                        forceCenter = null
                        val overlay = myLocationOverlayRef.value
                        val myLoc = overlay?.myLocation
                        if (myLoc != null) {
                            mapViewRef.value?.controller?.animateTo(myLoc)
                        } else {
                            val loc = state.currentUserLocation
                            if (loc != null) {
                                mapViewRef.value?.controller?.animateTo(
                                    GeoPoint(loc.first, loc.second)
                                )
                            }
                        }
                    }) {
                        Icon(Icons.Default.LocationSearching, contentDescription = null)
                    }
                    FloatingActionButton(onClick = { mapViewRef.value?.controller?.zoomIn() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                    FloatingActionButton(onClick = { mapViewRef.value?.controller?.zoomOut() }) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                    }
                    ExtendedFloatingActionButton(
                        onClick = {
                            val overlay = myLocationOverlayRef.value
                            val myLoc = overlay?.myLocation
                            if (myLoc != null) {
                                viewModel.onAddNewPointClicked(myLoc.latitude, myLoc.longitude)
                            } else {
                                val loc = state.currentUserLocation
                                if (loc != null) {
                                    viewModel.onAddNewPointClicked(loc.first, loc.second)
                                }
                            }
                        },
                        containerColor = Color(0xFF1B5E20),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) },
                        text = { Text(stringResource(R.string.add_place)) }
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    placeholder = {
                        Text(stringResource(R.string.search_places), fontSize = 13.sp)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                val filteredPoints by remember {
                    derivedStateOf {
                        val query = state.searchQuery.trim().lowercase()
                        if (query.isEmpty()) state.userPoints
                        else state.userPoints.filter { point ->
                            val speciesName = state.species
                                .find { it.id == point.speciesId }?.name?.lowercase() ?: ""
                            speciesName.contains(query) || point.userName.lowercase()
                                .contains(query)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredPoints, key = { it.id }) { point ->
                        val displayName = when {
                            point.userName.isNotBlank() && point.speciesId == 0 -> point.userName
                            point.speciesId == 0 -> stringResource(R.string.custom_species)
                            else -> state.species.find { it.id == point.speciesId }?.name
                                ?: point.userName.ifBlank {
                                    stringResource(R.string.unknown_species)
                                }
                        }

                        PointListItem(
                            point = point,
                            speciesName = displayName,
                            isSelected = point.id == state.selectedPointId,
                            onClick = {
                                viewModel.onPointClicked(point)
                                shouldFollowLocation = false
                                forceCenter = GeoPoint(point.latitude, point.longitude)
                            },
                            onLongClick = { viewModel.onPointLongClicked(point) },
                            onEdit = { viewModel.onPointLongClicked(point) },
                            onDelete = { viewModel.deletePoint(point.id) },
                            onShare = {
                                viewModel.sharePoint(context, point, displayName)
                            }
                        )
                    }
                }
            }
        }

        state.bottomSheetMode?.let { mode ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissBottomSheet() },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                when (mode) {
                    is BottomSheetMode.Add -> AddPointSheetContent(
                        species = state.species,
                        initialName = state.deepLinkName,
                        initialCategory = state.deepLinkCategory,
                        onSave = { speciesId: Int?, userName, description, category ->
                            viewModel.addNewPoint(
                                mode.latitude,
                                mode.longitude,
                                speciesId,
                                userName,
                                description,
                                category
                            )
                            viewModel.dismissBottomSheet()
                        },
                        onDismiss = { viewModel.dismissBottomSheet() }
                    )
                    is BottomSheetMode.Edit -> {
                        val point = state.userPoints.find { it.id == mode.pointId }
                        if (point != null) {
                            EditPointSheetContent(
                                point = point,
                                species = state.species,
                                onSave = { speciesId, userName, description, category ->
                                    viewModel.updateUserPoint(
                                        mode.pointId,
                                        speciesId,
                                        userName,
                                        description,
                                        category
                                    )
                                    viewModel.dismissBottomSheet()
                                },
                                onDelete = {
                                    viewModel.deletePoint(mode.pointId)
                                    viewModel.dismissBottomSheet()
                                },
                                onDismiss = { viewModel.dismissBottomSheet() }
                            )
                        }
                    }
                }
            }
        }
    }
}