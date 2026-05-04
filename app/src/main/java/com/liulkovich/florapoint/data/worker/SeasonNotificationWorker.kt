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
import com.liulkovich.florapoint.domain.Reference
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class SeasonNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FloraRepository,
    private val prefs: NotificationPreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "flora_season_channel"
    }

    override suspend fun doWork(): Result {
        createChannel()

        val notifyStart = prefs.notifyStart.first()
        val notifyPeak  = prefs.notifyPeak.first()
        val notifyEnd   = prefs.notifyEnd.first()

        if (!notifyStart && !notifyPeak && !notifyEnd) return Result.success()

        val today = Calendar.getInstance()
        val month = today.get(Calendar.MONTH) + 1
        val day   = today.get(Calendar.DAY_OF_MONTH)

        val species = repository.getNotificationEnabled()
        species.forEach { ref ->
            checkAndNotify(ref, month, day, notifyStart, notifyPeak, notifyEnd)
        }

        return Result.success()
    }

    private fun checkAndNotify(
        ref: Reference,
        month: Int,
        day: Int,
        notifyStart: Boolean,
        notifyPeak: Boolean,
        notifyEnd: Boolean
    ) {
        val start = ref.startMonth
        val end   = ref.endMonth

        val prevMonth        = if (start == 1) 12 else start - 1
        val lastDayOfPrev    = daysInMonth(prevMonth)
        val peakMonth        = calcPeakMonth(start, end)
        val lastDayOfSeason  = daysInMonth(end)
        val weekBeforeEndDay = lastDayOfSeason - 7

        when {
            notifyStart
                    && month == prevMonth
                    && day == lastDayOfPrev -> {
                sendNotification(
                    id    = ref.id * 10 + 1,
                    title = "Завтра открывается сезон!",
                    text  = "${ref.name} — подготовьтесь к выходу на природу."
                )
            }
            notifyStart
                    && month == start
                    && day == 1 -> {
                sendNotification(
                    id    = ref.id * 10 + 2,
                    title = "Сезон открыт: ${ref.name}",
                    text  = "Самое время проверить свои точки на карте!"
                )
            }
            notifyPeak
                    && month == peakMonth
                    && day == 1 -> {
                sendNotification(
                    id    = ref.id * 10 + 3,
                    title = "Пик сезона: ${ref.name}",
                    text  = "Сейчас самое урожайное время. Не пропустите!"
                )
            }
            notifyEnd
                    && month == end
                    && day == weekBeforeEndDay -> {
                sendNotification(
                    id    = ref.id * 10 + 4,
                    title = "Сезон заканчивается: ${ref.name}",
                    text  = "До конца сезона осталась неделя."
                )
            }
        }
    }

    private fun calcPeakMonth(start: Int, end: Int): Int {
        return if (end >= start) {
            (start + end) / 2
        } else {
            val span = (12 - start) + end
            ((start - 1 + span / 2) % 12) + 1
        }
    }

    private fun daysInMonth(month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun sendNotification(id: Int, title: String, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Сезоны флоры",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о начале, пике и конце сезона сбора"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}