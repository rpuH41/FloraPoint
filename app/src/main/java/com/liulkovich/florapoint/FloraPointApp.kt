package com.liulkovich.florapoint

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class FloraPointApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = Configuration.getInstance()
        config.load(this, getSharedPreferences("osm_config", MODE_PRIVATE))

        config.userAgentValue = "com.liulkovich.florapoint"
    }
}