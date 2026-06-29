    package com.liulkovich.florapoint.presentation.components

    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.offset
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.List
    import androidx.compose.material.icons.filled.Home
    import androidx.compose.material.icons.filled.LocationOn
    import androidx.compose.material.icons.filled.Settings
    import androidx.compose.material3.FloatingActionButton
    import androidx.compose.material3.Icon
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.platform.LocalDensity
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.liulkovich.florapoint.R
    import androidx.compose.foundation.layout.navigationBarsPadding

    data class BottomNavItem(
        val label: String,
        val icon: ImageVector,
        val route: String
    )

    @Composable
    fun BottomBar(
        currentRoute: String,
        onNavigate: (String) -> Unit,
        onForecastClick: () -> Unit
    ) {
        val items = listOf(
            BottomNavItem(stringResource(R.string.home), Icons.Default.Home, "Home"),
            BottomNavItem(stringResource(R.string.map), Icons.Default.LocationOn, "Map"),
            BottomNavItem(stringResource(R.string.guide), Icons.AutoMirrored.Filled.List, "Guide"),
            BottomNavItem(stringResource(R.string.settings), Icons.Default.Settings, "Settings"),
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.take(2).forEach { item ->
                        NavigationBarItemContent(
                            item = item,
                            selected = currentRoute.startsWith(item.route),
                            onClick = { onNavigate(item.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box(modifier = Modifier.width(64.dp))

                    items.drop(2).forEach { item ->
                        NavigationBarItemContent(
                            item = item,
                            selected = currentRoute.startsWith(item.route),
                            onClick = { onNavigate(item.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onForecastClick,
                shape = CircleShape,
                containerColor = Color(0xFF1B5E20),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (10).dp)
                    .size(56.dp)
            ) {
                Text(text = "🍄", fontSize = 24.sp)
            }
        }
    }

    @Composable
    private fun NavigationBarItemContent(
        item: BottomNavItem,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val density = LocalDensity.current
        val color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant

        Column(
            modifier = modifier
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = item.label,
                color = color,
                fontSize = with(density) { (12.sp.value / fontScale).sp },
                maxLines = 1
            )
        }
    }