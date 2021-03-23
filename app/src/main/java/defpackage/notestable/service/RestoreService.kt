package defpackage.notestable.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import defpackage.notestable.db
import defpackage.notestable.model.Event
import defpackage.notestable.model.Status
import defpackage.notestable.receiver.AlarmReceiver
import org.jetbrains.anko.intentFor

class RestoreService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val notes = db.notesDao().selectAll()
        notes.forEach { note ->
            if (note.status != Status.FREE) {
                AlarmReceiver.schedule(applicationContext, note.id, Event.EVERYDAY, note.updated)
            }
            if (note.status == Status.WAIT_OFFICE) {
                note.date?.let {
                    AlarmReceiver.schedule(applicationContext, note.id, Event.OFFICE_DATE, it)
                }
            }
        }
    }

    companion object {

        fun launch(context: Context) {
            with(context) {
                enqueueWork(
                    applicationContext,
                    RestoreService::class.java,
                    200,
                    intentFor<RestoreService>()
                )
            }
        }
    }
}