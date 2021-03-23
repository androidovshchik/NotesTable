package defpackage.notestable

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import defpackage.notestable.model.Note
import defpackage.notestable.model.Status
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.ZonedDateTime

@Dao
abstract class NotesDao {

    @Query(
        """
        SELECT * FROM notes
    """
    )
    abstract fun listenAll(): Flow<List<Note>>

    @Query(
        """
        SELECT * FROM notes
    """
    )
    abstract fun selectAll(): List<Note>

    @Query(
        """
        SELECT * FROM notes
        WHERE id = :id
        LIMIT 1
    """
    )
    abstract fun selectById(id: Long): Note?

    @Insert
    abstract fun insert(note: Note): Long

    @Query(
        """
        UPDATE notes
        SET status = :status, date = NULL, updated = :updated, days = 0
        WHERE id = :id
    """
    )
    abstract fun updateStatus(id: Long, status: Status, updated: ZonedDateTime)

    @Query(
        """
        UPDATE notes
        SET `desc` = :desc
        WHERE id = :id
    """
    )
    abstract fun updateDesc(id: Long, desc: String?)

    @Query(
        """
        UPDATE notes
        SET name = :name
        WHERE id = :id
    """
    )
    abstract fun updateName(id: Long, name: String?)

    @Query(
        """
        UPDATE notes
        SET note = :note
        WHERE id = :id
    """
    )
    abstract fun updateNote(id: Long, note: String?)

    @Query(
        """
        UPDATE notes
        SET tinged = :tinged
        WHERE id = :id
    """
    )
    abstract fun updateTinged(id: Long, tinged: Boolean)

    @Query(
        """
        UPDATE notes
        SET phone = :phone
        WHERE id = :id
    """
    )
    abstract fun updatePhone(id: Long, phone: Boolean)

    @Query(
        """
        UPDATE notes
        SET date = :date
        WHERE id = :id
    """
    )
    abstract fun updateDate(id: Long, date: ZonedDateTime?)

    @Query(
        """
        UPDATE notes
        SET days = :days
        WHERE id = :id
    """
    )
    abstract fun updateDays(id: Long, days: Int)

    open fun updateNote(id: Long, field: String, value: Any?) {
        when (field) {
            Note.STATUS -> updateStatus(id, value as Status, ZonedDateTime.now())
            Note.DESC -> updateDesc(id, value as String?)
            Note.NAME -> updateName(id, value as String?)
            Note.NOTE -> updateNote(id, value as String?)
            Note.TINGED -> updateTinged(id, value as Boolean)
            Note.PHONE -> updatePhone(id, value as Boolean)
            Note.DATE -> updateDate(id, value as ZonedDateTime?)
            Note.DAYS -> updateDays(id, value as Int)
            else -> throw NotImplementedError("Add implementation for field=$field")
        }
    }

    @Query(
        """
        UPDATE notes
        SET days = days + 1
        WHERE id = :id
    """
    )
    abstract fun incrementDays(id: Long)

    @Delete
    abstract fun delete(note: Note)
}