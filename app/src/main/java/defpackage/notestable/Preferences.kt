package defpackage.notestable

import android.content.Context
import com.chibatching.kotpref.KotprefModel
import defpackage.notestable.extension.getColorOf

class Preferences(context: Context) : KotprefModel(context) {

    override val kotprefName = "${context.packageName}_preferences"

    var waitOfficeColor by intPref(context.getColorOf(R.color.md_purple_500), "wait_office_color")

    var thereColor by intPref(context.getColorOf(R.color.md_green_500), "there_color")

    var orderColor by intPref(context.getColorOf(R.color.md_yellow_500), "order_color")

    var turnBackColor by intPref(context.getColorOf(R.color.md_brown_500), "turn_back_color")

    var freeColor by intPref(context.getColorOf(android.R.color.white), "free_color")

    var thereDays by stringPref("4,6", "there_days")
}