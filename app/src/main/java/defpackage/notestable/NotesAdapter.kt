package defpackage.notestable

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import defpackage.notestable.model.Note
import defpackage.notestable.model.Status
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.min
import kotlin.math.pow

class NotesAdapter(listener: Listener) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    private val reference = WeakReference(listener)

    val colors = EnumMap<Status, Int>(Status::class.java)

    val items = mutableListOf<Note>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_note, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = items[position]
        with(viewHolder) {
            val context = itemView.context.applicationContext
            val color = colors.getOrDefault(item.status, Color.BLACK)
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (item.status == Status.TURN_BACK) R.color.md_grey_300 else R.color.md_grey_100
                )
            )
            desc.text = item.desc
            name.text = item.name
            note.text = item.note
            date.isVisible = item.status == Status.WAIT_OFFICE
            date.text = item.date?.format(dateFormatter)
            tinged.setCheckedProgrammatically(item.tinged)
            phone.setCheckedProgrammatically(item.phone)
            status.text = item.status.title
            status.setTextColor(if (ColorUtils.calculateLuminance(color) < 0.5f) Color.WHITE else Color.BLACK)
            status.backgroundTintList = ColorStateList.valueOf(
                if (item.status == Status.THERE) {
                    val ratio = min(1f, (item.days / DANGER_DAYS).pow(2))
                    ColorUtils.blendARGB(color, Color.BLACK, ratio)
                } else color
            )
            days.isVisible = item.status != Status.FREE
            days.text = item.days.toString()
        }
    }

    override fun getItemCount() = items.size

    @Suppress("MemberVisibilityCanBePrivate")
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val desc: TextView = view.findViewById(R.id.tv_desc)
        val name: TextView = view.findViewById(R.id.tv_name)
        val note: TextView = view.findViewById(R.id.tv_note)
        val date: TextView = view.findViewById(R.id.tv_date)
        val tinged: ProgramCheckBox = view.findViewById(R.id.cb_tinged)
        val phone: ProgramCheckBox = view.findViewById(R.id.cb_phone)
        val status: MaterialButton = view.findViewById(R.id.mb_status)
        val days: TextView = view.findViewById(R.id.tv_days)

        init {
            desc.setOnClickListener(this)
            name.setOnClickListener(this)
            note.setOnClickListener(this)
            date.setOnClickListener(this)
            tinged.setOnCheckedChangeListener { _, isChecked ->
                val item = items[adapterPosition]
                if (!tinged.hasCheckedProgrammatically) {
                    reference.get()?.changeFlag(item.id, isChecked, Note.TINGED)
                }
            }
            phone.setOnCheckedChangeListener { _, isChecked ->
                val item = items[adapterPosition]
                if (!phone.hasCheckedProgrammatically) {
                    reference.get()?.changeFlag(item.id, isChecked, Note.PHONE)
                }
            }
            status.setOnClickListener(this)
            days.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            val item = items[position]
            val listener = reference.get() ?: return
            when (v.id) {
                R.id.tv_desc -> {
                    listener.changeText(item.id, item.desc, Note.DESC, "Описание")
                }
                R.id.tv_name -> {
                    listener.changeText(item.id, item.name, Note.NAME, "Имя")
                }
                R.id.tv_note -> {
                    listener.changeText(item.id, item.note, Note.NOTE, "Заметка")
                }
                R.id.tv_date -> {
                    listener.changeDate(item.id, item.date)
                }
                R.id.mb_status -> {
                    listener.changeStatus(item.id, item.status)
                }
                R.id.tv_days -> {
                    listener.changeNumber(item.id, item.days, Note.DAYS, "Дни")
                }
            }
        }
    }

    interface Listener {

        fun changeText(id: Long, value: String?, field: String, title: String)

        fun changeNumber(id: Long, value: Int, field: String, title: String)

        fun changeFlag(id: Long, value: Boolean, field: String)

        fun changeDate(id: Long, value: ZonedDateTime?)

        fun changeStatus(id: Long, value: Status)
    }

    companion object {

        private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}