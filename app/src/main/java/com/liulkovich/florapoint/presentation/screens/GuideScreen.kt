package com.liulkovich.florapoint.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.presentation.ui.theme.FloraPointTheme

@Composable
fun GuideScreen(
    modifier: Modifier = Modifier,
    viewModel: GuideViewModel = hiltViewModel(),
){

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
            ) {
                    Spacer(modifier = Modifier.height(30.dp))
                    Title(
                        modifier = Modifier
                            .padding(horizontal = 24.dp),
                        text = "Справочник"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SearchBar(
                        modifier = Modifier
                            .padding(horizontal = 24.dp),
                        query = state.query,
                        onQueryChange = {
                            viewModel.processCommand(GuideCommand.InputSearchQuery(it))
                        }
                    )
                    PanelFilter(
                        onCategoryChange = { categoryName ->
                            viewModel.processCommand(GuideCommand.CheckCategory(categoryName))
                        }
                    )
                }
            }
        ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding
        ) {

            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            items(state.species) {speciesItem ->
                GuideCard(
                    modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    textImage = speciesItem.imageName,
                    textName = speciesItem.name,
                    startMonth = speciesItem.startMonth,
                    endMonth = speciesItem.endMonth,
                    onNatifChange = { isChecked ->
                        viewModel.processCommand(
                            GuideCommand.ToggleNotification(
                                id = speciesItem.id,
                                enabled = if (isChecked) 1 else 0
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun Title(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 24.sp,
        fontWeight = Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
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
                text = "Ввидите название",
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
                contentDescription = "Search notes",
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
    onNatifChange: (Boolean) -> Unit
){
    val context = LocalContext.current
    val imageId = context.resources.getIdentifier(
        textImage,
        "drawable",
        context.packageName
    )
        Card(
            modifier = modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row() {
                Image(
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                    painter = if (imageId != 0) {
                        painterResource(id = imageId)
                    } else {
                        painterResource(id = R.drawable.ic_launcher_background)
                    },
                    contentDescription = null,
                    )
                Column(
                    modifier = Modifier
                        .weight(2f)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(10.dp),
                        text = textName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = Bold
                    )
                    Text(
                        modifier = Modifier
                            .padding( start = 10.dp ),
                        text = "Период: c ${numberInString(startMonth).first} по ${numberInString(endMonth).second}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                    )
                }
                var selectedNotif by remember { mutableStateOf(false) }

                IconToggleButton(
                    checked = selectedNotif,
                    onCheckedChange = { isChecked ->
                        selectedNotif = isChecked
                        onNatifChange(isChecked)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Уведомления",
                        tint = if (selectedNotif) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
}

fun numberInString(number: Int): Pair<String, String>{
    val month:Pair<String, String> = when(number){
        1 -> "января" to "январь"
        2 -> "февраля" to "февраль"
        3 -> "марта" to "март"
        4 -> "апреля" to "апрель"
        5 -> "мая" to "май"
        6 -> "июня" to "июнь"
        7 -> "июля" to "июль"
        8 -> "августа" to "август"
        9 -> "сентября" to "сентябрь"
        10 -> "октября" to "октябрь"
        11 -> "ноября" to "ноябрь"
        12 -> "декабря" to "декабрь"
        else -> "Неизвестно" to "Неизвестно"
    }
    return month
}
@Composable
fun PanelFilter(
    onCategoryChange: (String) -> Unit
){
        val categories = listOf("Грибы", "Ягоды", "Растения", "Орехи")
        val selectedFilters = remember { mutableStateListOf<String>() }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            categories.forEach { name ->
                val isSelected = selectedFilters.contains(name)

                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedFilters.remove(name)
                            onCategoryChange(name)
                        } else {
                            selectedFilters.add(name)
                            onCategoryChange(name)
                        }
                    },
                    label = { Text(name) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview(){
    FloraPointTheme {
        Column {
            // Передаем реальную строку для картинки
            GuideCard(
                textImage = "boletus",
                textName = "Белый гриб",
                startMonth = 6,
                endMonth = 9,
                onNatifChange = {}
            )
        }
    }
}