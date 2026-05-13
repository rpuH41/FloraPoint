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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPointSheetContent(
    species: List<Reference>,
    initialName: String = "",
    initialCategory: String = "",
    onSave: (speciesId: Int?, userName: String, description: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {


    var selectedCategory by remember { mutableStateOf(FloraCategory.MUSHROOM) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    val initialFlora = FloraCategory.fromKey(initialCategory) ?: FloraCategory.MUSHROOM
   // var selectedCategory by remember { mutableStateOf(initialFlora) }
    var searchText by remember { mutableStateOf(initialName) }
    val filteredSpecies = remember(species, selectedCategory) {
        species.filter { it.category == selectedCategory.key }
    }

    var selectedSpecies by remember(filteredSpecies) { mutableStateOf<Reference?>(null) }
  // var searchText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val suggestions = remember(searchText, filteredSpecies, selectedSpecies) {
        if (searchText.length < 2 || selectedSpecies != null) emptyList()
        else filteredSpecies.filter {
            it.name.lowercase().contains(searchText.lowercase().trim())
        }
    }

    SheetContent(title = stringResource(R.string.new_location)) {

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
                            selectedSpecies = null
                            searchText = ""
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
            placeholder = {
                Text(
                    text = filteredSpecies.firstOrNull()?.name?.let {
                        stringResource(
                            R.string.example,
                            it
                        ) }
                        ?: stringResource(R.string.enter_a_name),
                    color = MaterialTheme.colorScheme.outline
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (suggestions.isNotEmpty() && selectedSpecies == null && searchText.length >= 2) {
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
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    val finalSpeciesId: Int? = when {
                        selectedSpecies != null -> selectedSpecies!!.id
                        else -> filteredSpecies.find {
                            it.name.equals(searchText.trim(), ignoreCase = true)
                        }?.id
                    }
                    onSave(
                        finalSpeciesId,
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