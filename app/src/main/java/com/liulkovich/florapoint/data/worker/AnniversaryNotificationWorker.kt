package com.liulkovich.florapoint.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.UserPoints
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class AnniversaryNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FloraRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "flora_anniversary_channel"
    }

    override suspend fun doWork(): Result {
        createChannel()
        val oneYearAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
        val twoWeeksWindow = TimeUnit.DAYS.toMillis(14)

        val oldPoints = repository.getAllUserPoints()
            .first()
            .filter { point ->
                val pointTime = point.timestamp * 1000L
                pointTime in (oneYearAgo - twoWeeksWindow)..(oneYearAgo + twoWeeksWindow)
            }

        oldPoints.forEach { point ->
            sendAnniversaryNotification(point)
        }

        return Result.success()
    }

    private fun sendAnniversaryNotification(point: UserPoints) {
        val speciesName = point.userName.ifBlank { context.getString(R.string.unknown_species) }

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notificationId = (point.id + 10000).hashCode()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.discovery_anniversary))
            .setContentText(context.getString(R.string.return_prompt, speciesName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.discovery_anniversaries_two),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.year_old_spots_reminders)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}