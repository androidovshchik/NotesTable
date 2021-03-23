package defpackage.notestable.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import defpackage.notestable.service.RestoreService

@SuppressLint("UnsafeProtectedBroadcastReceiver")
class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        RestoreService.launch(context)
    }
}