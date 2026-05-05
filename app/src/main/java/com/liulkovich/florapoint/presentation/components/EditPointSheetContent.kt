package com.liulkovich.florapoint.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import com.liulkovich.florapoint.presentation.screens.map.FLORA_TYPES
import com.liulkovich.florapoint.presentation.screens.map.categoryForType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPointSheetContent(
    point: UserPoints,
    species: List<Reference>,
    onSave: (speciesId: Int?, userName: String, description: String, category: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val existingRef = species.find { it.id == point.speciesId }
    val currentType = FLORA_TYPES.firstOrNull {
        categoryForType(it) == existingRef?.category
    } ?: FLORA_TYPES.first()

    var selectedType by remember(point) { mutableStateOf(currentType) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val filteredSpecies = remember(species, selectedType) {
        species.filter { it.category == categoryForType(selectedType) }
    }

    var searchText by remember(point) {
        mutableStateOf(
            point.userName.ifBlank { existingRef?.name ?: "" }
        )
    }
    var selectedSpecies by remember(point, filteredSpecies) {
        mutableStateOf(filteredSpecies.find { it.id == point.speciesId })
    }

    val suggestions = remember(searchText, filteredSpecies, selectedSpecies) {
        if (searchText.length < 2 || selectedSpecies != null) emptyList()
        else filteredSpecies.filter {
            it.name.lowercase().contains(searchText.lowercase().trim())
        }
    }

    var description by remember(point) { mutableStateOf(point.description) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateStr = remember(point.timestamp) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(point.timestamp.toLong() * 1000))
    }

    SheetContent(title = "Редактировать точку") {

        Text(
            text = "Добавлено: $dateStr",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = typeDropdownExpanded,
            onExpandedChange = { typeDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = typeDropdownExpanded,
                onDismissRequest = { typeDropdownExpanded = false }
            ) {
                FLORA_TYPES.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            typeDropdownExpanded = false
                            searchText = ""
                            selectedSpecies = null
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                if (selectedSpecies?.name != it) selectedSpecies = null
            },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                when {
                    selectedSpecies != null ->
                        Text("✓ Совпадает со справочником", color = MaterialTheme.colorScheme.primary)
                    searchText.isNotBlank() && suggestions.isEmpty() && searchText.length >= 2 ->
                        Text("Не найдено в справочнике — сохранится как своё название",
                            color = MaterialTheme.colorScheme.outline)
                    else -> {}
                }
            }
        )

        if (suggestions.isNotEmpty() && selectedSpecies == null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp,
                shadowElevation = 2.dp
            ) {
                Column {
                    suggestions.take(5).forEachIndexed { index, ref ->
                        if (index > 0) HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    ref.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                searchText = ref.name
                                selectedSpecies = ref
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            placeholder = { Text("Место, особенности, заметки...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = {
                    val matched = selectedSpecies
                        ?: filteredSpecies.find { it.name.equals(searchText.trim(), ignoreCase = true) }

                    val selectedCategory = categoryForType(selectedType)

                    onSave(matched?.id, searchText.trim(), description.trim(), selectedCategory)
                },
                modifier = Modifier.weight(1f),
                enabled = searchText.isNotBlank()
            ) {
                Text("Сохранить")
            }
        }
    }

}