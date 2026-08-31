package com.liulkovich.florapoint.presentation.screens.map

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.localizedName
import com.liulkovich.florapoint.presentation.components.AddPointSheetContent
import com.liulkovich.florapoint.presentation.components.EditPointSheetContent
import com.liulkovich.florapoint.presentation.components.OsmMapView
import com.liulkovich.florapoint.presentation.components.PointListItem
import com.liulkovich.florapoint.presentation.weather.WeatherViewModel
import kotlinx.coroutines.flow.collectLatest
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.compose.ui.focus.FocusManager
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel,
    deepLinkLat: Double? = null,
    deepLinkLon: Double? = null,
    deepLinkName: String? = null,
    deepLinkCategory: String? = null,
    mapFocusHolder: MapFocusRequestHolder,
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val myLocationOverlayRef = remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var shouldFollowLocation by remember { mutableStateOf(true) }
    var forceCenter by remember { mutableStateOf<GeoPoint?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val pendingFocusId by mapFocusHolder.pendingFocusPointId.collectAsStateWithLifecycle()

    LaunchedEffect(pendingFocusId, state.userPoints) {
        val id = pendingFocusId ?: return@LaunchedEffect
        val point = state.userPoints.find { it.id == id } ?: return@LaunchedEffect
        viewModel.onPointClicked(point)
        shouldFollowLocation = false
        forceCenter = GeoPoint(point.latitude, point.longitude)
        mapFocusHolder.consume()
    }

    LaunchedEffect(deepLinkLat, deepLinkLon) {
        if (deepLinkLat != null && deepLinkLon != null && deepLinkLat != 0.0) {
            shouldFollowLocation = false
            forceCenter = GeoPoint(deepLinkLat, deepLinkLon)
            viewModel.onAddNewPointClicked(deepLinkLat, deepLinkLon)
            deepLinkName?.let { viewModel.setDeepLinkData(it, deepLinkCategory ?: "custom") }
        }
    }

    val sharedLocation by weatherViewModel.currentLocation.collectAsStateWithLifecycle()

    LaunchedEffect(sharedLocation) {
        sharedLocation?.let { (lat, lon) ->
            viewModel.updateCurrentLocation(lat, lon)
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
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                MapContent(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .padding(16.dp),
                       // .clip(RoundedCornerShape(16.dp)),
                    state = state,
                    mapPoints = mapPoints,
                    viewModel = viewModel,
                    mapViewRef = mapViewRef,
                    myLocationOverlayRef = myLocationOverlayRef,
                    shouldFollowLocation = shouldFollowLocation,
                    forceCenter = forceCenter,
                    onShouldFollowLocationChange = { shouldFollowLocation = it },
                    onForceCenterChange = { forceCenter = it },
                    focusManager = focusManager,
                    keyboardController = keyboardController,
                    isLandscape = true
                )

                VerticalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                PointsListContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    state = state,
                    viewModel = viewModel,
                    context = context,
                    focusManager = focusManager,
                    keyboardController = keyboardController,
                    onShouldFollowLocationChange = { shouldFollowLocation = it },
                    onForceCenterChange = { forceCenter = it }
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                MapContent(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .padding(16.dp),
                       // .clip(RoundedCornerShape(16.dp)),
                    state = state,
                    mapPoints = mapPoints,
                    viewModel = viewModel,
                    mapViewRef = mapViewRef,
                    myLocationOverlayRef = myLocationOverlayRef,
                    shouldFollowLocation = shouldFollowLocation,
                    forceCenter = forceCenter,
                    onShouldFollowLocationChange = { shouldFollowLocation = it },
                    onForceCenterChange = { forceCenter = it },
                    focusManager = focusManager,
                    keyboardController = keyboardController
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                PointsListContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = state,
                    viewModel = viewModel,
                    context = context,
                    focusManager = focusManager,
                    keyboardController = keyboardController,
                    onShouldFollowLocationChange = { shouldFollowLocation = it },
                    onForceCenterChange = { forceCenter = it }
                )
            }
        }

        state.bottomSheetMode?.let { mode ->
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
                        onOpenSettings = { onOpenSettings() }
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
                                onOpenSettings = { onOpenSettings() }
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
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
private fun MapContent(
    modifier: Modifier,
    state: MapScreenState,
    mapPoints: List<com.liulkovich.florapoint.domain.UserPoints>,
    viewModel: MapViewModel,
    mapViewRef: MutableState<MapView?>,
    myLocationOverlayRef: MutableState<MyLocationNewOverlay?>,
    shouldFollowLocation: Boolean,
    forceCenter: GeoPoint?,
    onShouldFollowLocationChange: (Boolean) -> Unit,
    onForceCenterChange: (GeoPoint?) -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    isLandscape: Boolean = false  // ← новый параметр
) {
    val fabSize = if (isLandscape) 40.dp else 56.dp
    val fabIconSize = if (isLandscape) 18.dp else 24.dp
    val fabSpacing = if (isLandscape) 4.dp else 8.dp
    val contentPadding = if (isLandscape) 8.dp else 16.dp
    val legendButtonHeight = if (isLandscape) 28.dp else 36.dp
    val legendButtonFontSize = if (isLandscape) 11.sp else 13.sp
    val legendIconSize = if (isLandscape) 12.dp else 16.dp

    Box(modifier = modifier) {
        OsmMapView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
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
                focusManager.clearFocus()
                keyboardController?.hide()

                if (viewModel.isPublicForeignPoint(point)) {
                    viewModel.onPublicPointClicked(point)
                } else {
                    viewModel.onPointClicked(point)
                }

                onShouldFollowLocationChange(false)
                onForceCenterChange(GeoPoint(point.latitude, point.longitude))
            },
            onMarkerLongClick = { point -> viewModel.onPointLongClicked(point) }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(fabSpacing),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = {
                    onShouldFollowLocationChange(true)
                    onForceCenterChange(null)
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
                modifier = Modifier.size(fabSize),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    Icons.Default.LocationSearching,
                    contentDescription = null,
                    modifier = Modifier.size(fabIconSize)
                )
            }
            FloatingActionButton(
                onClick = { mapViewRef.value?.controller?.zoomIn() },
                modifier = Modifier.size(fabSize),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(fabIconSize))
            }
            FloatingActionButton(
                onClick = { mapViewRef.value?.controller?.zoomOut() },
                modifier = Modifier.size(fabSize),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(fabIconSize))
            }

            LegendGroupBox(
                title = stringResource(R.string.place)
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
                        modifier = Modifier.height(legendButtonHeight),
                        contentPadding = PaddingValues(horizontal = if (isLandscape) 8.dp else 12.dp)
                    ) {
                        Icon(
                            Icons.Default.AddLocationAlt,
                            contentDescription = null,
                            modifier = Modifier.size(legendIconSize)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.add_place),
                            fontSize = legendButtonFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (viewModel.isAuthorized()) {
                        Spacer(Modifier.width(if (isLandscape) 4.dp else 8.dp))
                        Button(
                            onClick = { viewModel.togglePointsFilter() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.showOnlyMyPoints)
                                    Color(0xFF1B5E20)
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(legendButtonHeight),
                            contentPadding = PaddingValues(horizontal = if (isLandscape) 8.dp else 12.dp)
                        ) {
                            Text(
                                text = if (state.showOnlyMyPoints)
                                    stringResource(R.string.filter_my_points)
                                else
                                    stringResource(R.string.filter_all_points),
                                fontSize = legendButtonFontSize,
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
}

@Composable
private fun PointsListContent(
    modifier: Modifier,
    state: MapScreenState,
    viewModel: MapViewModel,
    context: android.content.Context,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    onShouldFollowLocationChange: (Boolean) -> Unit,
    onForceCenterChange: (GeoPoint?) -> Unit
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            placeholder = {
                Text(stringResource(R.string.search_places), fontSize = 13.sp)
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        val displayPoints = remember(
            state.sortedPointsWithDistance,
            state.searchQuery
        ) {
            val query = state.searchQuery.trim().lowercase()
            if (query.isEmpty()) {
                state.sortedPointsWithDistance
            } else {
                state.sortedPointsWithDistance.filter { (point, _) ->
                    val speciesName = state.species
                        .find { it.id == point.speciesId }?.localizedName()?.lowercase() ?: ""
                    speciesName.contains(query) || point.userName.lowercase().contains(query)
                }
            }
        }
        val listState = rememberLazyListState()

        LaunchedEffect(state.selectedPointId) {
            val selectedIndex = displayPoints.indexOfFirst { (point, _) ->
                point.id == state.selectedPointId
            }
            if (selectedIndex >= 0) {
                listState.animateScrollToItem(selectedIndex)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            state = listState,
        ) {
            items(
                items = displayPoints,
                key = { (point, _) ->
                    if (point.id != 0) "local_${point.id}"
                    else "public_${point.cloudId ?: point.hashCode()}"
                }
            ) { (point, distance) ->
                val displayName = when {
                    point.userName.isNotBlank() && point.speciesId == 0 -> point.userName
                    point.speciesId == 0 -> stringResource(R.string.custom_species)
                    else -> state.species.find { it.id == point.speciesId }?.localizedName()
                        ?: point.userName.ifBlank { stringResource(R.string.unknown_species) }
                }

                PointListItem(
                    point = point,
                    speciesName = displayName,
                    distance = distance,
                    isSelected = point.id == state.selectedPointId,
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        if (!viewModel.isPublicForeignPoint(point)) {
                            viewModel.onPointClicked(point)
                        }
                        onShouldFollowLocationChange(false)
                        onForceCenterChange(GeoPoint(point.latitude, point.longitude))
                    },
                    isOwner = !viewModel.isPublicForeignPoint(point),
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
            lineHeight = 12.sp,
            color = Color.Black,
            style = LocalTextStyle.current.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            modifier = Modifier
                .padding(start = 12.dp)
                .background(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 1.dp, vertical = 0.dp)
                .align(Alignment.TopStart)
        )
    }
}