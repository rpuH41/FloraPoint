package com.liulkovich.florapoint.domain

import androidx.annotation.StringRes
import com.liulkovich.florapoint.R

enum class FloraCategory(
    @StringRes val stringRes: Int,
    val key: String,
    val imageName: String,
    val emoji: String
) {
    MUSHROOM(R.string.mushrooms, "mushroom", "mushroom", "🍄"),
    BERRY(R.string.berries, "berry", "berry",  "🫐"),
    PLANT(R.string.plants, "plant", "plant", "🌿"),
    NUT(R.string.nuts, "nut", "nut", "🌰");

    companion object {
        fun fromKey(key: String): FloraCategory? =
            entries.find { it.key == key }

        fun fromDisplayName(displayName: String, getString: (Int) -> String): FloraCategory? =
            entries.find { getString(it.stringRes) == displayName }
    }
}