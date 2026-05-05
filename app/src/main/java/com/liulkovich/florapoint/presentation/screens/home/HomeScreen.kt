package com.liulkovich.florapoint.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.Tip
import com.liulkovich.florapoint.presentation.ui.theme.FloraPointTheme
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onClickMap: () -> Unit,
    onClickCategory: (String) -> Unit,
    onClickDetail: (Int) -> Unit
){
    val state by viewModel.state.collectAsState()


    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp,0.dp,0.dp),
    ){ innerPadding ->

            LazyColumn(
                contentPadding = innerPadding
            ) {
                item {
                    Box(modifier = Modifier.padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        )) {
                        MapPanel(onClickMap = onClickMap)
                    }
                }
                item {
                    Title(name = "Совет")
                }
                item {
                    state.tip?.let { tip ->
                        TipOfTheDayCard(tip = tip)
                    } ?: Spacer(modifier = Modifier.height(0.dp))
                }
                item {
                    Title(
                        name = "Сезон сейчас"
                    )
                }
                item {
                    val rows = if (state.species.size <= 2) 1 else 2

                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(rows),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(if (rows == 1) 105.dp else 220.dp) // высота тоже меняется
                    ) {
                        items(state.species) { speciesItem ->
                            HomeSeasonCard(
                                textImage = speciesItem.imageName,
                                textName = speciesItem.name,
                                endMonth = speciesItem.endMonth,
                                reference = speciesItem,
                                onNatifChange = { },
                                onClickDetail = { onClickDetail(speciesItem.id) },
                            )
                        }
                    }
                }
                item {
                    Title(
                        name = "Справочники"
                    )
                }
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(320.dp), // высота под 2 ряда
                        userScrollEnabled = false // скролл у LazyColumn, не у grid
                    ) {
                        items(listOf(
                            Pair("Грибы", "mushroom"),
                            Pair("Ягоды", "berry"),
                            Pair("Растения", "plant"),
                            Pair("Орехи", "nut"),
                        )) { (name, image) ->
                            TypeFlora(
                                modifier = Modifier,
                                nameTypes = name,
                                nameImage = image,
                                onClickType = { onClickCategory(name) }
                            )
                        }
                    }
                }

            }
    }
}

@Composable
fun TypeCard(
    nameTypes: String,
    nameImage: String,
){
    val context = LocalContext.current
    val imageId = context.resources.getIdentifier(nameImage, "drawable", context.packageName)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            painter = if (imageId != 0) painterResource(id = imageId)
            else painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = nameTypes,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TypeFlora(
    modifier: Modifier,
    nameTypes: String,
    nameImage: String,
    onClickType: () -> Unit
) {
    val context = LocalContext.current
    val imageId = context.resources.getIdentifier(nameImage, "drawable", context.packageName)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onClickType() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                fontWeight = Bold,
                textAlign = TextAlign.Center
            )
        }
    }
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
    val diff = endCal.timeInMillis - today.timeInMillis
    return diff / (1000 * 60 * 60 * 24)
}
@Composable
fun HomeSeasonCard(
    modifier: Modifier = Modifier,
    textImage: String,
    textName: String,
    endMonth: Int,
    reference: Reference,
    onNatifChange: (Boolean) -> Unit,
    onClickDetail:(Reference) -> Unit,
){
    val context = LocalContext.current
    val imageId = context.resources
        .getIdentifier(
            textImage,
            "drawable",
            context.packageName
        )
    Card(
        modifier = modifier.width(185.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onClickDetail(reference) }
    ) {
        Column {
            Box {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    painter = if (imageId != 0) painterResource(id = imageId)
                    else painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                var selectedNotif by remember { mutableStateOf(reference.isNotifEnabled == 1) }
                IconToggleButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    checked = selectedNotif,
                    onCheckedChange = { isChecked ->
                        selectedNotif = isChecked
                        onNatifChange(isChecked)
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
                fontWeight = Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                text = "Осталось ~${countDay(endMonth)} дней",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun Title(
    name: String
){
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        fontSize = 20.sp,
        fontWeight = Bold,
        modifier = Modifier
            .padding(
                top = 5.dp,
                bottom = 5.dp,
                start = 16.dp
            )
    )
}

@Composable
fun MapPanel(
    onClickMap: () -> Unit
) {
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
                .clickable(
                    onClick = onClickMap,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Открыть карту",
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
        // цвет фона теперь такой же, как у других карточек (surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Icon(
//                imageVector = Icons.Default.Lightbulb,
//                contentDescription = "Совет",
//                tint = MaterialTheme.colorScheme.primary
//            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tip.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview(){
    FloraPointTheme {
        Column {
            Title(
                "Сезон сейчас "
            )
        }
    }
}