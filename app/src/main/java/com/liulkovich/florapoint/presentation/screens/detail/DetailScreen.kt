package com.liulkovich.florapoint.presentation.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.presentation.screens.guide.numberInString
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    isNotificationEnabled: Boolean = false,
    onNotificationToggle: (Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    Scaffold(

        topBar = {

            TopAppBar(
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                title = {
                    Text(
                        text = state.species?.name ?: "",
                        fontWeight = Bold,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth(),
                            //.background(color = Color.Black.copy(alpha = 0.3f)),
                        textAlign = TextAlign.Center

                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier

                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    var notifEnabled by remember { mutableStateOf(state.isNotificationEnabled) }
                    LaunchedEffect(state.isNotificationEnabled) {
                        notifEnabled = state.isNotificationEnabled
                    }
                    IconToggleButton(
                        checked = notifEnabled,
                        onCheckedChange = { isChecked ->
                            notifEnabled = isChecked
                            onNotificationToggle(isChecked)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Уведомления",
                            tint = if (notifEnabled) Color(0xFF66BB6A)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(Color.Transparent)
            )
        }
    ) { innerPadding ->
        state.species?.let { species ->
            LazyColumn(
                contentPadding = innerPadding
            ) {
                item { HeroImage(species.imageName, species.name) }
                item { InfoSection(species) }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onNotificationToggle(!state.isNotificationEnabled) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isNotificationEnabled) Color(0xFF66BB6A) else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (state.isNotificationEnabled) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = if (state.isNotificationEnabled) "Отключить уведомления" else "Включить уведомления"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HeroImage(imageName: String, name: String) {
    val context = LocalContext.current
    val imageId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Image(
            painter = if (imageId != 0) painterResource(imageId)
            else painterResource(R.drawable.ic_launcher_background),
            contentDescription = "Изображения гриба, ягоды, растения, ореха",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                )
                .clip(RoundedCornerShape(16.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()

        )
        Text(
            text = name,
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = Bold,

            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 26.dp,
                    bottom = 16.dp
                )
        )
    }
}

@Composable
fun InfoSection(species: Reference) {
    Column(modifier = Modifier.padding(16.dp)) {

        InfoBlock(
            icon = Icons.Default.LocationOn,
            title = "Где растёт",
            text = species.habitat
        )
        InfoBlock(
            icon = Icons.Default.Info,
            title = "Описание",
            text = species.description
        )
        InfoBlock(
            icon = Icons.Default.Warning,
            title = "Похожие виды",
            text = species.lookAlikes
        )
        InfoBlock(
            icon = Icons.Default.DateRange,
            title = "Период сбора",
            text = "С ${numberInString(species.startMonth).first} по ${numberInString(species.endMonth).second}"
        )
    }
}

@Composable
fun InfoBlock(icon: ImageVector, title: String, text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                //tint = Color(0xFF2E7D32),
                modifier = Modifier.padding(end = 12.dp, top = 2.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = Bold,
                    style = MaterialTheme.typography.titleSmall,

                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

