package defpackage.notestable

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import defpackage.notestable.extension.isMarshmallowPlus
import defpackage.notestable.model.Event
import defpackage.notestable.model.Note
import defpackage.notestable.model.Status
import defpackage.notestable.receiver.AlarmReceiver
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.powerManager
import org.jetbrains.anko.startActivityForResult
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime

class MainActivity : AppCompatActivity(), NotesAdapter.Listener, SwipeCallback.Listener {

    private lateinit var preferences: Preferences

    private val adapter = NotesAdapter(this)

    private val listener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            when {
                dy > 0 -> fab_add.hide()
                dy < 0 -> fab_add.show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = Preferences(applicationContext)
        setContentView(R.layout.activity_main)
        updateColors()
        rv_notes.adapter = adapter
        rv_notes.addItemDecoration(DividerItemDecoration(applicationContext, VERTICAL))
        ItemTouchHelper(SwipeCallback(this, applicationContext))
            .attachToRecyclerView(rv_notes)
        rv_notes.addOnScrollListener(listener)
        fab_add.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val item = Note(ZonedDateTime.now())
                    db.notesDao().insert(item)
                }
            }
        }
        lifecycleScope.launch {
            db.notesDao().listenAll().collect {
                adapter.items.clear()
                adapter.items.addAll(it.sorted())
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isMarshmallowPlus()) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                startActivityForResult(
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 1
                )
            }
        }
    }

    override fun onSwiped(position: Int) {
        val note = adapter.items[position]
        if (note.status != Status.FREE) {
            AlarmReceiver.cancel(applicationContext, note.id, Event.EVERYDAY)
        }
        if (note.status == Status.WAIT_OFFICE) {
            if (note.date != null) {
                AlarmReceiver.cancel(applicationContext, note.id, Event.OFFICE_DATE)
            }
        }
        deleteNote(note)
    }

    override fun changeText(id: Long, value: String?, field: String, title: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(View.inflate(applicationContext, R.layout.dialog_input, null))
            .setNegativeButton(getString(android.R.string.cancel), null)
            .setPositiveButton(getString(android.R.string.ok), null)
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.setOnShowListener {
            val input: EditText = dialog.findViewById(R.id.et_input)!!
            input.setText(value)
            input.setSelection(input.text.length)
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                updateNote(id, field, input.text.toString())
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun changeNumber(id: Long, value: Int, field: String, title: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(View.inflate(applicationContext, R.layout.dialog_wheel, null))
            .setNegativeButton(getString(android.R.string.cancel), null)
            .setPositiveButton(getString(android.R.string.ok), null)
            .create()
        dialog.setOnShowListener {
            val wheel: NumberPicker = dialog.findViewById(R.id.np_wheel)!!
            wheel.wrapSelectorWheel = false
            wheel.minValue = 0
            wheel.maxValue = 3650
            wheel.value = value
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                updateNote(id, field, wheel.value)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun changeFlag(id: Long, value: Boolean, field: String) {
        updateNote(id, field, value)
    }

    override fun changeDate(id: Long, value: ZonedDateTime?) {
        val oldDate = value ?: ZonedDateTime.now()
        val dialog = DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                val date = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                val time = oldDate.toLocalTime()
                val newDate = ZonedDateTime.of(date, time, oldDate.zone)
                AlarmReceiver.schedule(applicationContext, id, Event.OFFICE_DATE, newDate)
                updateNote(id, Note.DATE, newDate)
            },
            DateTimeUtils.toGregorianCalendar(oldDate)
        )
        dialog.setTitle("Дата")
        dialog.show(supportFragmentManager, "date")
    }

    override fun changeStatus(id: Long, value: Status) {
        val array = Status.values()
        var index = array.indexOf(value)
        AlertDialog.Builder(this)
            .setTitle("Статус")
            .setSingleChoiceItems(array.map { it.title }.toTypedArray(), index) { _, which ->
                index = which
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val now = ZonedDateTime.now()
                val context = applicationContext
                AlarmReceiver.cancel(context, id, Event.OFFICE_DATE)
                if (array[index] != Status.FREE) {
                    AlarmReceiver.schedule(context, id, Event.EVERYDAY, now)
                }
                updateNote(id, Note.STATUS, array[index])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    private fun updateColors() {
        with(adapter.colors) {
            put(Status.WAIT_OFFICE, preferences.waitOfficeColor)
            put(Status.THERE, preferences.thereColor)
            put(Status.ORDER, preferences.orderColor)
            put(Status.TURN_BACK, preferences.turnBackColor)
            put(Status.FREE, preferences.freeColor)
        }
    }

    private fun updateNote(id: Long, field: String, value: Any?) {
        GlobalScope.launch(Dispatchers.IO) {
            db.notesDao().updateNote(id, field, value)
        }
    }

    private fun deleteNote(note: Note) {
        GlobalScope.launch(Dispatchers.IO) {
            db.notesDao().delete(note)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivityForResult<SettingsActivity>(100)
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            updateColors()
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        rv_notes.removeOnScrollListener(listener)
        super.onDestroy()
    }
}