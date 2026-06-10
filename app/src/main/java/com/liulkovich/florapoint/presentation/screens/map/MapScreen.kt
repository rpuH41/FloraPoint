package com.liulkovich.florapoint.presentation.screens.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.localizedName
import com.liulkovich.florapoint.presentation.components.AddPointSheetContent
import com.liulkovich.florapoint.presentation.components.EditPointSheetContent
import com.liulkovich.florapoint.presentation.components.OsmMapView
import com.liulkovich.florapoint.presentation.components.PointListItem
import kotlinx.coroutines.flow.collectLatest
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    deepLinkLat: Double? = null,
    deepLinkLon: Double? = null,
    deepLinkName: String? = null,
    deepLinkCategory: String? = null,
    onOpenSettings: () -> Unit = {}
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
    LaunchedEffect(state.currentUserLocation) {
        state.currentUserLocation?.let { (lat, lon) ->
            viewModel.loadCurrentWeather(lat, lon)
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
        viewModel.updateMissingWeatherData()
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

    val mapPoints = remember(
        state.userPoints,
        state.publicPoints,
        state.showOnlyMyPoints
    ) {

        if (!viewModel.isAuthorized()) {

            state.userPoints

        } else {

            if (state.showOnlyMyPoints) {
                state.userPoints
            } else {
                state.userPoints + state.publicPoints
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
                    points = mapPoints,
                    species = state.species,
                    selectedPointId = state.selectedPointId,
                    currentLocation = state.currentUserLocation,
                    shouldFollowLocation = shouldFollowLocation,
                    forceCenter = forceCenter,
                    currentWeather = state.currentWeather,
                    onMapReady = { mapView, locationOverlay ->
                        mapView.setBuiltInZoomControls(false)
                        mapViewRef.value = mapView
                        myLocationOverlayRef.value = locationOverlay
                    },
                    onMarkerClick = { point ->

                        if (viewModel.isPublicForeignPoint(point)) {
                            viewModel.onPublicPointClicked(point)
                        } else {
                            viewModel.onPointClicked(point)
                        }

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
                    },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.LocationSearching, contentDescription = null)
                    }
                    FloatingActionButton(
                        onClick = { mapViewRef.value?.controller?.zoomIn() },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer

                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                    FloatingActionButton(
                        onClick = { mapViewRef.value?.controller?.zoomOut() },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                    }

                    LegendGroupBox(
                        title = "Place",
                       // modifier = Modifier.fillMaxWidth()//.padding(horizontal = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Button(
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1B5E20),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddLocationAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.add_place),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (viewModel.isAuthorized()) {
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.togglePointsFilter() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.showOnlyMyPoints)
                                            Color(0xFF1B5E20)
                                        else
                                            MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = if (state.showOnlyMyPoints)
                                            stringResource(R.string.filter_my_points)
                                        else
                                            stringResource(R.string.filter_all_points),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (state.showOnlyMyPoints)
                                            Color.White
                                        else
                                            MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
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
                                .find { it.id == point.speciesId }?.localizedName()?.lowercase() ?: ""
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
                            else -> state.species.find { it.id == point.speciesId }?.localizedName()
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

            val configuration = LocalConfiguration.current
            val currentLocale = configuration.locales[0]
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
                        onSave = { speciesId: Int?, userName, description, category, isPublic ->
                            viewModel.addNewPoint(
                                mode.latitude,
                                mode.longitude,
                                speciesId,
                                userName,
                                description,
                                category,
                                isPublic
                            )
                            viewModel.dismissBottomSheet()
                        },
                        onDismiss = { viewModel.dismissBottomSheet() },
                        isAuthorized = viewModel.isAuthorized(),
                        onOpenSettings = {
                            onOpenSettings()
                        }
                    )
                    is BottomSheetMode.Edit -> {
                        val point = state.userPoints.find { it.id == mode.pointId }
                        if (point != null) {
                            EditPointSheetContent(
                                point = point,
                                species = state.species,
                                onSave = { speciesId, userName, description, category, isPublic ->
                                    viewModel.updateUserPoint(
                                        mode.pointId,
                                        speciesId,
                                        userName,
                                        description,
                                        category,
                                        isPublic
                                    )
                                    viewModel.dismissBottomSheet()
                                },
                                onDelete = {
                                    viewModel.deletePoint(mode.pointId)
                                    viewModel.dismissBottomSheet()
                                },
                                onDismiss = { viewModel.dismissBottomSheet() },
                                isAuthorized = viewModel.isAuthorized(),
                                onOpenSettings = {
                                    onOpenSettings()
                                }
                            )
                        }
                    }

                    is BottomSheetMode.ViewPublic -> {

                        val point = mode.point

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = point.userName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )

                                    Spacer(Modifier.width(4.dp))

                                    Text(
                                        text = SimpleDateFormat(
                                            "dd MMM yyyy",
                                            currentLocale
                                        ).format(Date(point.timestamp * 1000)),
                                        color = MaterialTheme.colorScheme.outline,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                if (point.description.isNotBlank()) {

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = point.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {

                                    point.temperature?.let {
                                        Text("☀️ ${it.toInt()}°")
                                    }

                                    point.humidity?.let {
                                        Text("💧 ${it}%")
                                    }

                                    point.avgTemp5Days?.let {
                                        Text("🌡️ Ø ${it.toInt()}°")
                                    }

                                    point.avgHumidity5Days?.let {
                                        Text("💧 Ø ${it}%")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendGroupBox(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                //.wrapContentSize()
                .padding(top = 8.dp)
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 15.dp)
        ) {
            content()
        }

        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(start = 12.dp)
                .background(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                //.padding(horizontal = 1.dp, vertical = 1.dp)
                .align(Alignment.TopStart)
        )
    }
}