package defpackage.notestable

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jakewharton.threetenabp.AndroidThreeTen
import defpackage.notestable.extension.isOreoPlus
import defpackage.notestable.model.Event
import org.jetbrains.anko.notificationManager
import timber.log.Timber

lateinit var db: Database

@Suppress("unused")
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        db = Room.databaseBuilder(applicationContext, Database::class.java, "app.db")
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .fallbackToDestructiveMigration()
            .build()
        AndroidThreeTen.init(this)
        if (isOreoPlus()) {
            Event.values().forEach {
                notificationManager.createNotificationChannel(
                    NotificationChannel(it.channel, it.title, IMPORTANCE_DEFAULT)
                )
            }
        }
    }
}