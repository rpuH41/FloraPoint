package com.liulkovich.florapoint.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.liulkovich.florapoint.R

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
        BottomNavItem(stringResource(R.string.home), Icons.Default.Home, "Home"),
        BottomNavItem(stringResource(R.string.map), Icons.Default.LocationOn, "Map"),
        BottomNavItem(stringResource(R.string.guide), Icons.AutoMirrored.Filled.List, "Guide"),
        BottomNavItem(stringResource(R.string.settings), Icons.Default.Settings, "Settings"),
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute.startsWith(item.route.substringBefore("?")),
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