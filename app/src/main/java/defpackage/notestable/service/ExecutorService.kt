package defpackage.notestable.service

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import defpackage.notestable.*
import defpackage.notestable.extension.pendingActivityFor
import defpackage.notestable.model.Event
import defpackage.notestable.model.Note
import defpackage.notestable.model.Status
import defpackage.notestable.receiver.AlarmReceiver
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

class ExecutorService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        val offset = intent.getIntExtra(EXTRA_OFFSET, 0)
        val note = db.notesDao().selectById(id) ?: return
        val event = Event.fromOffset(offset)
        Timber.i("Executing note id=$id event=$event")
        when (event) {
            Event.EVERYDAY -> {
                db.notesDao().incrementDays(id)
                if (note.status != Status.FREE) {
                    val context = applicationContext
                    AlarmReceiver.schedule(context, note.id, Event.EVERYDAY, note.updated)
                }
                if (note.status == Status.ORDER) {
                    val now = ZonedDateTime.now()
                    val daysAgo = note.updated.until(now, ChronoUnit.DAYS)
                    Timber.i("Everyday order id=$id days ago $daysAgo")
                    if (BuildConfig.DEBUG || daysAgo >= 2) {
                        showNotification(note, event, note.desc?.substringAfter(" "))
                    }
                }
            }
            Event.OFFICE_DATE -> {
                if (note.status == Status.WAIT_OFFICE) {
                    showNotification(note, event, "ПОРА!!!")
                }
            }
        }
    }

    private fun showNotification(note: Note, event: Event, text: String?) {
        val title = note.desc?.substringBefore(" ").orEmpty()
        val builder = NotificationCompat.Builder(applicationContext, event.channel)
            .setSmallIcon(R.drawable.ic_baseline_announcement_24)
            .setContentTitle(title)
            .setContentText(text.orEmpty())
            .setContentIntent(pendingActivityFor<MainActivity>())
            .setSubText(note.name)
            .setGroup(event.group)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        notificationManager.notify(event.offset + note.id.toInt(), builder.build())
        val groupBuilder = NotificationCompat.Builder(applicationContext, event.channel)
            .setSmallIcon(R.drawable.ic_baseline_announcement_24)
            .setContentTitle(event.title)
            .setContentText(title)
            .setGroup(event.group)
            .setGroupSummary(true)
        notificationManager.notify(event.offset, groupBuilder.build())
    }

    companion object {

        fun launch(context: Context, extras: Bundle) {
            with(context) {
                enqueueWork(
                    applicationContext,
                    ExecutorService::class.java,
                    100,
                    intentFor<ExecutorService>().also {
                        it.putExtras(extras)
                    }
                )
            }
        }
    }
}