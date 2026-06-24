package com.liulkovich.florapoint.presentation.screens.forecast

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.ForecastItem
import com.liulkovich.florapoint.domain.localizedName
import com.liulkovich.florapoint.presentation.screens.map.CurrentWeather

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel = hiltViewModel(),
    currentWeather: CurrentWeather?,
    currentLat: Double,
    currentLon: Double,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToMapWithPoint: (Int?) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(currentWeather) {
        viewModel.load(currentWeather, currentLat, currentLon)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(
                text = stringResource(R.string.forecast_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.forecast_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (!state.noWeather && !state.isLoading) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                   // horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.weather_now),
                        //color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "☀️ ${state.temperature}°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "💧 ${state.humidity}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                   // horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.weather_last_5days),
                       // color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "🌡️ ${state.avgTemp.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "💧 ${state.avgHumidity}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.noWeather -> {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.no_weather_data), color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            state.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_suitable_species), color = MaterialTheme.colorScheme.outline)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.items, key = { it.reference.id }) { item ->
                        ForecastCard(
                            item = item,
                            onCardClick = { onNavigateToDetail(item.reference.id) },
                            onLocationClick = {
                                onNavigateToMapWithPoint(item.nearbyPointId)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastCard(
    item: ForecastItem,
    onCardClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    val (color, emoji) = when {
        item.score >= 80 -> Color(0xFF1BA226) to "🍄"
        item.score >= 50 -> Color(0xFFF9A825) to "🍄"
        else -> Color(0xFFE65100) to "🍄"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.reference.localizedName(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when {
                        item.score >= 80 -> stringResource(R.string.forecast_excellent)
                        item.score >= 50 -> stringResource(R.string.forecast_good)
                        else -> stringResource(R.string.forecast_weak)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }

            if (item.hasNearbyPoint) {
                IconButton(onClick = onLocationClick) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Показать на карте",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}