package defpackage.notestable.model

import java.util.*

// do not change without reinstallation of app
const val MAX_CAPACITY = Int.MAX_VALUE / 10

/**
 * @param offset Prior to [Int.MAX_VALUE]
 */
enum class Event(val offset: Int, val title: String) {
    EVERYDAY(0, "Заказы"),
    OFFICE_DATE(MAX_CAPACITY, "Ждут офис");

    val channel = "channel_${name.toLowerCase(Locale.getDefault())}"

    val group = "group_${name.toLowerCase(Locale.getDefault())}"

    companion object {

        private val map = values().associateBy(Event::offset)

        fun fromOffset(value: Int) = map[value]
            ?: throw IllegalArgumentException("Unknown event offset")
    }
}