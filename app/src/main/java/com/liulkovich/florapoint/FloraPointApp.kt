package com.liulkovich.florapoint

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.liulkovich.florapoint.data.worker.SeasonNotificationWorker
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.work.Configuration as WorkConfiguration

@HiltAndroidApp
class FloraPointApp : Application(), WorkConfiguration.Provider {

    @Inject
    lateinit var workerConfiguration: WorkConfiguration

    override val workManagerConfiguration: WorkConfiguration
        get() = workerConfiguration

    override fun onCreate() {
        super.onCreate()

        val config = Configuration.getInstance()
        config.load(this, getSharedPreferences("osm_config", MODE_PRIVATE))
        config.userAgentValue = "com.liulkovich.florapoint"

        config.osmdroidBasePath = File(filesDir, "osmdroid")
        config.osmdroidTileCache = File(filesDir, "osmdroid/tiles")

        config.tileFileSystemCacheMaxBytes = 500L * 1024 * 1024
        config.tileFileSystemCacheTrimBytes = 400L * 1024 * 1024

        scheduleSeasonWorker()
    }

    private fun scheduleSeasonWorker() {

        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now) || now.get(Calendar.HOUR_OF_DAY) >= 12) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<SeasonNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "season_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )


    }
}