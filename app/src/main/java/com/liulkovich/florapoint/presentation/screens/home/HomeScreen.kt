package com.liulkovich.florapoint.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.FloraCategory
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.Tip
import com.liulkovich.florapoint.domain.localizedName
import com.liulkovich.florapoint.domain.localizedText
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onClickMap: () -> Unit,
    onClickCategory: (String) -> Unit,
    onClickDetail: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    ) { innerPadding ->

        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                ) {
                    MapPanel(onClickMap = onClickMap)
                }
            }

            item { Title(name = stringResource(R.string.tip)) }
            item {
                state.tip?.let { tip ->
                    TipOfTheDayCard(tip = tip)
                }
            }

            item { Title(name = stringResource(R.string.reference)) }
            item {
                ReferenceCategories(onClickCategory = onClickCategory)
            }

            item { Title(name = stringResource(R.string.season_now)) }
            item {
                SeasonSection(
                    state = state,
                    viewModel = viewModel,
                    onClickDetail = onClickDetail
                )
            }
            item {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun SeasonSection(
    state: HomeScreenState,
    viewModel: HomeViewModel,
    onClickDetail: (Int) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(225.dp)
            .padding(horizontal = 16.dp)
    ) {
        when {
            state.isLoading -> {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(4) { SeasonCardSkeleton() }
                }
            }

            state.species.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_active_seasons),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                val rows = if (state.species.size <= 2) 1 else 2

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(rows),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.species, key = { it.id }) { speciesItem ->
                        HomeSeasonCard(
                            imageName = speciesItem.imageName,   // ← передаём имя
                            textName = speciesItem.localizedName(),
                            endMonth = speciesItem.endMonth,
                            reference = speciesItem,
                            onNotificationChange = { isChecked ->
                                viewModel.toggleNotification(speciesItem.id, isChecked)
                            },
                            onClickDetail = { onClickDetail(speciesItem.id) },
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SeasonCardSkeleton() {
    Card(
        modifier = Modifier
            .width(165.dp)
            .height(150.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(110.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 8.dp)
                    .width(80.dp)
                    .height(14.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
fun ReferenceCategories(onClickCategory: (String) -> Unit) {
    val visibleCategories = FloraCategory.entries.filter { it != FloraCategory.OTHER }
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        visibleCategories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    val imageId = remember(category.imageName) {
                        context.resources.getIdentifier(
                            category.imageName, "drawable", context.packageName
                        )
                    }
                    TypeFlora(
                        modifier = Modifier.weight(1f),
                        nameTypes = stringResource(category.stringRes),
                        imageId = imageId,
                        onClickType = { onClickCategory(category.key) }
                    )
                }
            }
        }
    }
}

@Composable
fun TypeFlora(
    modifier: Modifier,
    nameTypes: String,
    imageId: Int,
    onClickType: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClickType
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(8.dp),
                painter = if (imageId != 0) painterResource(id = imageId)
                else painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = nameTypes,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HomeSeasonCard(
    modifier: Modifier = Modifier,
    imageName: String,           // ← теперь имя, а не id
    textName: String,
    endMonth: Int,
    reference: Reference,
    onNotificationChange: (Boolean) -> Unit,
    onClickDetail: (Int) -> Unit,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(165.dp)
            .height(150.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onClickDetail(reference.id) }
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(
                            context.resources.getIdentifier(
                                imageName, "drawable", context.packageName
                            ).takeIf { it != 0 } ?: R.drawable.ic_launcher_background
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                var selectedNotif by remember(reference.id) {
                    mutableStateOf(reference.isNotifEnabled == 1)
                }

                IconToggleButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    checked = selectedNotif,
                    onCheckedChange = { isChecked ->
                        selectedNotif = isChecked
                        onNotificationChange(isChecked)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (selectedNotif) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                modifier = Modifier.padding(start = 8.dp, top = 6.dp, end = 8.dp),
                text = textName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier.padding(start = 8.dp, /*bottom = 2.dp,*/ end = 8.dp),
                text = stringResource(R.string.days_left, countDay(endMonth)),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MapPanel(onClickMap: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = {
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(false)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(53.133562, 25.141006))
                }
            },
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(onClick = onClickMap),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.open_map),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TipOfTheDayCard(tip: Tip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tip.localizedText(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun Title(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}

fun countDay(endMonth: Int): Long {
    val today = java.util.Calendar.getInstance()
    val endCal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.MONTH, endMonth - 1)
        set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
        set(java.util.Calendar.HOUR_OF_DAY, 23)
        set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59)
    }
    val diffDays = (endCal.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)
    return if (diffDays < 0) 0 else diffDays
}