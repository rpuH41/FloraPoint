package com.liulkovich.florapoint.data.worker

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "notification_prefs")

@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val NOTIFY_START = booleanPreferencesKey("notify_start")
        val NOTIFY_PEAK  = booleanPreferencesKey("notify_peak")
        val NOTIFY_END   = booleanPreferencesKey("notify_end")
    }

    val notifyStart: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[NOTIFY_START] ?: true }

    val notifyPeak: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[NOTIFY_PEAK] ?: true }

    val notifyEnd: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[NOTIFY_END] ?: true }

    suspend fun setNotifyStart(enabled: Boolean) =
        context.dataStore.edit { it[NOTIFY_START] = enabled }

    suspend fun setNotifyPeak(enabled: Boolean) =
        context.dataStore.edit { it[NOTIFY_PEAK] = enabled }

    suspend fun setNotifyEnd(enabled: Boolean) =
        context.dataStore.edit { it[NOTIFY_END] = enabled }
}