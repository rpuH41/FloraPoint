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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.FloraCategory
import com.liulkovich.florapoint.domain.Reference
import com.liulkovich.florapoint.domain.UserPoints
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPointSheetContent(
    point: UserPoints,
    species: List<Reference>,
    onSave: (speciesId: Int?, userName: String, description: String, category: String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val existingRef = species.find { it.id == point.speciesId }

    val initialCategory = FloraCategory.fromKey(
        existingRef?.category ?: point.category ?: FloraCategory.MUSHROOM.key
    ) ?: FloraCategory.MUSHROOM

    var selectedCategory by remember(point) { mutableStateOf(initialCategory) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val filteredSpecies = remember(species, selectedCategory) {
        species.filter { it.category == selectedCategory.key }
    }

    var searchText by remember(point) {
        mutableStateOf(existingRef?.name ?: point.userName.ifBlank { "" })
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

    val dateStr = remember(point.timestamp) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(point.timestamp * 1000L))
    }

    SheetContent(title = stringResource(R.string.edit_point)) {
        Text(
            text = stringResource(R.string.added, dateStr),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = typeDropdownExpanded,
            onExpandedChange = { typeDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = stringResource(selectedCategory.stringRes),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = typeDropdownExpanded,
                onDismissRequest = { typeDropdownExpanded = false }
            ) {
                FloraCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(stringResource(category.stringRes)) },
                        onClick = {
                            selectedCategory = category
                            typeDropdownExpanded = false
                            if (selectedSpecies?.category != category.key) {
                                selectedSpecies = null
                                //searchText = ""
                            }
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
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,

        )

        if (suggestions.isNotEmpty() && selectedSpecies == null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
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
            label = { Text(stringResource(R.string.description)) },
            placeholder = { Text(stringResource(R.string.location_features_notes)) },
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
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    val matched = selectedSpecies
                        ?: filteredSpecies.find {
                            it.name.equals(searchText.trim(), ignoreCase = true)
                        }
                    onSave(
                        matched?.id,
                        searchText.trim(),
                        description.trim(),
                        selectedCategory.key
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = searchText.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}