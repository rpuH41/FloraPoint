package com.liulkovich.florapoint.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.UserPoints
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import com.liulkovich.florapoint.domain.LocationUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PointListItem(
    point: UserPoints,
    speciesName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isOwner: Boolean = true,
    distance: Double? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val dateStr = remember(point.timestamp) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(point.timestamp * 1000))
    }

    val pointKey = if (point.id != 0) "local_${point.id}"
    else "public_${point.cloudId ?: point.hashCode().toString()}"

    var expanded by remember(pointKey) { mutableStateOf(false) }
    var showDeleteDialog by remember(pointKey) { mutableStateOf(false) }

    LaunchedEffect(isSelected) {
        if (isSelected) expanded = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    expanded = !expanded
                    onClick()
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                !isOwner -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF66BB6A),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = speciesName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .weight(1f)
                )
                if (distance != null) {
                    Text(
                        text = LocationUtils.formatDistance(distance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    //color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Icon(
                    imageVector = if (expanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    if (point.description.isNotBlank()) {
                        Text(
                            text = point.description,
                            style = MaterialTheme.typography.bodySmall,
                            //color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    WeatherInlineRow(
                        point = point,
                        isSelected = isSelected,
                        dateStr = dateStr,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Share, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                        }
                        if (isOwner) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null,
                                    modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_this_point)) },
            text = { Text(stringResource(R.string.this_action_cannot_be_undone)) },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun WeatherInlineRow(
    point: UserPoints,
    isSelected: Boolean,
    dateStr: String,
    modifier: Modifier = Modifier
) {
    if (
        point.temperature != null &&
        point.humidity != null &&
        point.avgTemp5Days != null &&
        point.avgHumidity5Days != null
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.weather_on_date, dateStr))
            WeatherChip(emoji = "☀️", label = "${point.temperature.toInt()}°", isSelected = isSelected)
            WeatherChip(emoji = "💧", label = "${point.humidity}%", isSelected = isSelected)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.weather_5days_before, dateStr))
            WeatherChip(emoji = "🌡️", label = "${point.avgTemp5Days.toInt()}°", isSelected = isSelected)
            WeatherChip(emoji = "💧", label = "${point.avgHumidity5Days}%", isSelected = isSelected)
        }
    } else {
        Text(
            text = stringResource(R.string.loadingl_eather),
            style = MaterialTheme.typography.labelSmall,
            modifier = modifier
        )
    }
}

@Composable
private fun WeatherChip(
    emoji: String,
    label: String,
    isSelected: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = " $label",
            style = MaterialTheme.typography.labelLarge,
            /*color = if (isSelected)
                Color.Black
            else
                MaterialTheme.colorScheme.onSurfaceVariant*/
            fontWeight = FontWeight.Medium
        )
    }
}