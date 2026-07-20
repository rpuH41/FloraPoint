package com.liulkovich.florapoint.domain

import java.util.Locale

fun Reference.localizedName(): String =
    if (Locale.getDefault().language == "en") nameEn?.takeIf { it.isNotBlank() } ?: nameRu
    else nameRu

fun Reference.localizedHabitat(): String =
    if (Locale.getDefault().language == "en") habitatEn?.takeIf { it.isNotBlank() } ?: habitatRu
    else habitatRu

fun Reference.localizedLookAlikes(): String =
    if (Locale.getDefault().language == "en") lookAlikesEn?.takeIf { it.isNotBlank() } ?: lookAlikesRu
    else lookAlikesRu

fun Reference.localizedDescription(): String =
    if (Locale.getDefault().language == "en") descriptionEn?.takeIf { it.isNotBlank() } ?: descriptionRu
    else descriptionRu

fun Reference.localizedDifferences(): String {
    val isRu = Locale.getDefault().language != "en"
    val ru = differencesRu ?: ""
    return if (isRu) ru else differencesEn?.takeIf { it.isNotBlank() } ?: ru
}