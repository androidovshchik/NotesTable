package defpackage.notestable

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import defpackage.notestable.model.Note
import defpackage.notestable.model.Status
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

@Database(
    entities = [
        Note::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun notesDao(): NotesDao
}

object Converters {

    @TypeConverter
    @JvmStatic
    fun fromStatus(value: Status?): String? {
        return value?.name
    }

    @TypeConverter
    @JvmStatic
    fun toStatus(value: String?): Status? {
        return value?.let {
            Status.valueOf(it)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromZonedDateTime(value: ZonedDateTime?): String? {
        return value?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @TypeConverter
    @JvmStatic
    fun toZonedDateTime(value: String?): ZonedDateTime? {
        return value?.let {
            ZonedDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
    }
}