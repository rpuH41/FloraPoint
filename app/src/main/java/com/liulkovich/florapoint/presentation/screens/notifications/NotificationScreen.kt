package com.liulkovich.florapoint.presentation.screens.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liulkovich.florapoint.presentation.screens.guide.GuideCard

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Уведомления о сезонах",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Выберите когда получать уведомления",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            NotificationSettingItem(
                title = "Начало сезона",
                subtitle = "За день до начала и в первый день",
                checked = state.notifyStart,
                onCheckedChange = viewModel::setNotifyStart
            )
            HorizontalDivider()
            NotificationSettingItem(
                title = "Пик сезона",
                subtitle = "В середине сезона — самое урожайное время",
                checked = state.notifyPeak,
                onCheckedChange = viewModel::setNotifyPeak
            )
            HorizontalDivider()
            NotificationSettingItem(
                title = "Конец сезона",
                subtitle = "За неделю до окончания сезона",
                checked = state.notifyEnd,
                onCheckedChange = viewModel::setNotifyEnd
            )
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (!state.notifyStart && !state.notifyPeak && !state.notifyEnd) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Все уведомления отключены",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Уведомления придут около полудня",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            HorizontalDivider()
            Spacer(Modifier.height(4.dp))

            Text(
                text = if (state.enabledSpecies.isEmpty()) "Нет видов с уведомлениями"
                else "Уведомления включены (${state.enabledSpecies.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (state.enabledSpecies.isEmpty()) {
            Text(
                text = "Включите уведомления в справочнике через значок колокольчика.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(state.enabledSpecies, key = { it.id }) { species ->
                    GuideCard(
                        modifier = Modifier.padding(vertical = 8.dp),
                        textImage = species.imageName,
                        textName = species.name,
                        startMonth = species.startMonth,
                        endMonth = species.endMonth,
                        reference = species,
                        onNotifChange = { isChecked ->
                            viewModel.toggleNotification(
                                id = species.id,
                                enabled = if (isChecked) 1 else 0
                            )
                        },
                        onClickDetail = {}
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun NotificationSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFF4CAF50)
            )
        )
    }
}