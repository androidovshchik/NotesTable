package defpackage.notestable

import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import org.jetbrains.anko.matchParent

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewId = View.generateViewId()
        setContentView(FrameLayout(applicationContext).apply {
            id = viewId
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
        })
        title = "Настройки"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction()
            .replace(viewId, SettingsFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val preferences = Preferences(requireContext())
        val thereDays = findPreference<EditTextPreference>("there_days")
        thereDays?.summary = preferences.thereDays
        thereDays?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
            it.keyListener = DigitsKeyListener.getInstance("0123456789,")
            it.setSelection(it.text.length)
        }
        thereDays?.setOnPreferenceChangeListener { preference, newValue ->
            preference.summary = newValue.toString()
            true
        }
    }
}