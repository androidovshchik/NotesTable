package defpackage.notestable.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.text.format.DateUtils
import androidx.core.app.AlarmManagerCompat
import defpackage.notestable.BuildConfig
import defpackage.notestable.EXTRA_ID
import defpackage.notestable.EXTRA_OFFSET
import defpackage.notestable.extension.pendingReceiverFor
import defpackage.notestable.model.Event
import defpackage.notestable.service.ExecutorService
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.intentFor
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import kotlin.math.max

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ExecutorService.launch(context, intent.extras!!)
    }

    companion object {

        fun schedule(context: Context, id: Long, event: Event, dateTime: ZonedDateTime) {
            cancel(context, id, event)
            val millis = when (event) {
                Event.EVERYDAY -> {
                    val now = LocalTime.now()
                    val time = dateTime.toLocalTime()
                    var duration = Duration.between(now, time).toMillis()
                    if (duration <= 0) {
                        duration += DateUtils.DAY_IN_MILLIS
                    }
                    if (BuildConfig.DEBUG) {
                        DateUtils.MINUTE_IN_MILLIS
                    } else {
                        duration
                    }
                }
                Event.OFFICE_DATE -> {
                    val now = ZonedDateTime.now()
                    if (now.isAfter(dateTime)) {
                        return
                    }
                    if (BuildConfig.DEBUG) {
                        DateUtils.MINUTE_IN_MILLIS * 3
                    } else {
                        Duration.between(now, dateTime).toMillis()
                    }
                }
            }
            Timber.i(
                """
                Scheduling alarm id=$id event=$event duration=${
                    Duration.of(
                        millis,
                        ChronoUnit.MILLIS
                    )
                }
            """.trimIndent()
            )
            with(context) {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + max(0L, millis),
                    newAlarm(id, event)
                )
            }
        }

        fun cancel(context: Context, id: Long, event: Event) {
            with(context) {
                alarmManager.cancel(newAlarm(id, event))
            }
        }

        private fun Context.newAlarm(id: Long, event: Event): PendingIntent {
            return pendingReceiverFor(intentFor<AlarmReceiver>().also {
                it.data = Uri.parse("$packageName://${event.offset}/$id")
                it.putExtra(EXTRA_ID, id)
                it.putExtra(EXTRA_OFFSET, event.offset)
            }, event.offset + id.toInt())
        }
    }
}