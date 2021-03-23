package defpackage.notestable

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.checkbox.MaterialCheckBox

class ProgramCheckBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.checkboxStyle
) : MaterialCheckBox(context, attrs, defStyleAttr) {

    var hasCheckedProgrammatically = false

    override fun setChecked(checked: Boolean) {
        hasCheckedProgrammatically = false
        super.setChecked(checked)
    }

    fun setCheckedProgrammatically(checked: Boolean) {
        hasCheckedProgrammatically = true
        super.setChecked(checked)
    }
}