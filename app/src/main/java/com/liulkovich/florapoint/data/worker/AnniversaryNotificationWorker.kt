package com.liulkovich.florapoint.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.liulkovich.florapoint.R
import com.liulkovich.florapoint.domain.FloraRepository
import com.liulkovich.florapoint.domain.UserPoints
import com.liulkovich.florapoint.domain.localizedName
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
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
        val twoWeeksWindow = TimeUnit.DAYS.toMillis(14) // ±7 дней от годовщины

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
        val speciesName = point.userName.ifBlank { "Неизвестный вид" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📍 Годовщина находки!")
            .setContentText("Год назад вы нашли $speciesName в этом месте. Не пора ли вернуться?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(point.id + 10000, notification) // уникальный ID
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Годовщины находок",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о точках, которым исполнился год"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}