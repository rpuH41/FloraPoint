package com.liulkovich.florapoint.presentation.screens.guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.FloraCategory
import com.liulkovich.florapoint.domain.Reference

@Composable
fun GuideScreen(
    modifier: Modifier = Modifier,
    viewModel: GuideViewModel = hiltViewModel(),
    onClickDetail: (Reference) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                SearchBar(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    query = state.query,
                    onQueryChange = {
                        viewModel.processCommand(GuideCommand.InputSearchQuery(it))
                    }
                )
                PanelFilter(
                    selectedCategories = state.selectedCategories,
                    onCategoryChange = { categoryName ->
                        viewModel.processCommand(GuideCommand.CheckCategory(categoryName))
                    }
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn(
                contentPadding = innerPadding
            ) {
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                items(state.species) { speciesItem ->
                    GuideCard(
                        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        textImage = speciesItem.imageName,
                        textName = speciesItem.name,
                        startMonth = speciesItem.startMonth,
                        endMonth = speciesItem.endMonth,
                        reference = speciesItem,
                        onNotifChange = { isChecked ->
                            viewModel.processCommand(
                                GuideCommand.ToggleNotification(
                                    id = speciesItem.id,
                                    enabled = if (isChecked) 1 else 0
                                )
                            )
                        },
                        onClickDetail = onClickDetail,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.enter_a_name),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun GuideCard(
    modifier: Modifier = Modifier,
    textImage: String,
    textName: String,
    startMonth: Int,
    endMonth: Int,
    reference: Reference,
    onNotifChange: (Boolean) -> Unit,
    onClickDetail: (Reference) -> Unit
) {
    val context = LocalContext.current
    val imageId = context.resources.getIdentifier(
        textImage,
        "drawable",
        context.packageName
    )

    var selectedNotif by remember(reference.isNotifEnabled) {
        mutableStateOf(reference.isNotifEnabled == 1)
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),

        onClick = { onClickDetail(reference) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                painter = if (imageId != 0)
                    painterResource(id = imageId)
                else
                    painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = textName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(
                        R.string.season,
                        numberInString(startMonth).first,
                        numberInString(endMonth).second
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconToggleButton(
                checked = selectedNotif,
                onCheckedChange = { isChecked ->
                    selectedNotif = isChecked
                    onNotifChange(isChecked)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (selectedNotif)
                        Color(0xFF4CAF50)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
fun numberInString(number: Int): Pair<String, String> {
    return when (number) {
        1 -> stringResource(R.string.from_january) to stringResource(R.string.january)
        2 -> stringResource(R.string.from_february) to stringResource(R.string.february)
        3 -> stringResource(R.string.from_march) to stringResource(R.string.march)
        4 -> stringResource(R.string.from_april) to stringResource(R.string.april)
        5 -> stringResource(R.string.from_may) to stringResource(R.string.may)
        6 -> stringResource(R.string.from_june) to stringResource(R.string.june)
        7 -> stringResource(R.string.from_july) to stringResource(R.string.july)
        8 -> stringResource(R.string.from_august) to stringResource(R.string.august)
        9 -> stringResource(R.string.from_september) to stringResource(R.string.september)
        10 -> stringResource(R.string.from_october) to stringResource(R.string.october)
        11 -> stringResource(R.string.from_november) to stringResource(R.string.november)
        12 -> stringResource(R.string.from_december) to stringResource(R.string.december)
        else -> stringResource(R.string.unknown) to stringResource(R.string.unknown)
    }
}

@Composable
fun PanelFilter(
    selectedCategories: Set<String>,
    onCategoryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        FloraCategory.entries.forEach { category ->
            val isSelected = selectedCategories.contains(category.key)
            FilterChip(
                selected = isSelected,
                onClick = { onCategoryChange(category.key) },
                label = {
                    Text(
                        text = "${category.emoji} ${stringResource(category.stringRes)}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}