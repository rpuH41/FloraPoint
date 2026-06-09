package com.liulkovich.florapoint.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liulkovich.florapoint.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

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

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            modifier = Modifier.height(100.dp),
            windowInsets = WindowInsets(0.dp),
           // containerColor = MaterialTheme.colorScheme.surface
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute.startsWith(item.route),
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        val density = LocalDensity.current
                        Text(
                            text = item.label,
                            fontSize = with(density) { (12.sp.value / fontScale).sp },
                            maxLines = 1
                        )
                    },
                    alwaysShowLabel = true
                )
            }
        }
    }
}