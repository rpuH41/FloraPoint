package com.liulkovich.florapoint.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem("Главная", Icons.Default.Home, "Home"),
        BottomNavItem("Карта", Icons.Default.LocationOn, "Map"),
        BottomNavItem("Справочник", Icons.Default.List, "Guide"),
        BottomNavItem("Уведомления", Icons.Default.Notifications, "Notifications"),
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute?.startsWith(item.route.substringBefore("?")) == true,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}