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
import com.liulkovich.florapoint.presentation.screens.map.FLORA_TYPES
import com.liulkovich.florapoint.presentation.screens.map.categoryForType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPointSheetContent(
    species: List<Reference>,
    onSave: (speciesId: Int?, userName: String, description: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(FLORA_TYPES.first()) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val filteredSpecies = remember(species, selectedType) {
        species.filter { it.category == categoryForType(selectedType) }
    }

    var selectedSpecies by remember(filteredSpecies) { mutableStateOf<Reference?>(null) }
    var searchText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val suggestions = remember(searchText, filteredSpecies) {
        if (searchText.length < 2) emptyList()
        else filteredSpecies.filter {
            it.name.lowercase().contains(searchText.lowercase().trim())
        }
    }

    SheetContent(title = "Новая точка") {

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
            placeholder = {
                Text(
                    text = filteredSpecies.firstOrNull()?.name?.let { "Например: $it" }
                        ?: "Введите название...",
                    color = MaterialTheme.colorScheme.outline
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                when {
                    selectedSpecies != null ->
                        Text("✓ Совпадает со справочником", color = MaterialTheme.colorScheme.primary)
                    searchText.isNotBlank() && suggestions.isEmpty() ->
                        Text("Не найдено в справочнике — сохранится как своё название",
                            color = MaterialTheme.colorScheme.outline)
                    else -> {}
                }
            }
        )

        if (suggestions.isNotEmpty() && selectedSpecies == null && searchText.length >= 2) {
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
                                    text = ref.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
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

        Spacer(Modifier.height(8.dp))

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
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Отмена")
            }
            Button(
                onClick = {
                    val finalSpeciesId: Int? = when {
                        selectedSpecies != null -> selectedSpecies!!.id
                        else -> {
                            val exactMatch = filteredSpecies.find {
                                it.name.equals(searchText.trim(), ignoreCase = true)
                            }
                            exactMatch?.id
                        }
                    }

                    val selectedCategory = categoryForType(selectedType)

                    onSave(finalSpeciesId, searchText.trim(), description.trim(), selectedCategory)
                },
                modifier = Modifier.weight(1f),
                enabled = searchText.isNotBlank()
            ) {
                Text("Сохранить")
            }
        }
    }
}