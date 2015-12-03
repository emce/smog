package mobi.cwiklinski.smog.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import mobi.cwiklinski.bloodline.ui.extension.IntentFor


class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent!!.action)) {
            context!!.startService(IntentFor<TimeService>(context));
        }
    }
}