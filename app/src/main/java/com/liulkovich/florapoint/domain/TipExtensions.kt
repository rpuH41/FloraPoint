package com.liulkovich.florapoint.domain

import java.util.Locale

fun Tip.localizedText(): String =
    if (Locale.getDefault().language == "en") textEn?.takeIf { it.isNotBlank() } ?: textRu
    else textRu