package defpackage.notestable.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.ZonedDateTime

@Entity(
    tableName = "notes"
)
class Note : Comparable<Note> {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    var id = 0L

    @ColumnInfo(name = STATUS)
    var status = Status.FREE
        set(value) {
            field = value
            date = null
            updated = ZonedDateTime.now()
            days = 0
        }

    @ColumnInfo(name = DESC)
    var desc: String? = null

    @ColumnInfo(name = NAME)
    var name: String? = null

    @ColumnInfo(name = NOTE)
    var note: String? = null

    @ColumnInfo(name = DATE)
    var date: ZonedDateTime? = null

    @ColumnInfo(name = TINGED)
    var tinged = false

    @ColumnInfo(name = PHONE)
    var phone = false

    @ColumnInfo(name = UPDATED)
    lateinit var updated: ZonedDateTime

    @ColumnInfo(name = DAYS)
    var days = 0

    constructor()

    constructor(created: ZonedDateTime) {
        updated = created
    }

    override fun compareTo(other: Note): Int {
        val result = compareValuesBy(this, other, { it.status.priority })
        if (result != 0) {
            return result
        }
        return compareValuesBy(other, this, { it.days })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Note
        if (id != other.id) return false
        if (status != other.status) return false
        if (desc != other.desc) return false
        if (name != other.name) return false
        if (note != other.note) return false
        if (date != other.date) return false
        if (tinged != other.tinged) return false
        if (phone != other.phone) return false
        if (updated != other.updated) return false
        if (days != other.days) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (desc?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (note?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + tinged.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + updated.hashCode()
        result = 31 * result + days
        return result
    }

    companion object {

        const val ID = "id"
        const val STATUS = "status"
        const val DESC = "desc"
        const val NAME = "name"
        const val NOTE = "note"
        const val DATE = "date"
        const val TINGED = "tinged"
        const val PHONE = "phone"
        const val UPDATED = "updated"
        const val DAYS = "days"
    }
}