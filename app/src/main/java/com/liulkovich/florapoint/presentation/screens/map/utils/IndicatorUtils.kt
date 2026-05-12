package com.liulkovich.florapoint.presentation.screens.map.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun isOnline(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = checkConnection(context)) {
        while (true) {
            delay(3000)
            value = checkConnection(context)
        }
    }
}

fun checkConnection(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}