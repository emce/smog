package mobi.cwiklinski.smog.service

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.RemoteViews
import mobi.cwiklinski.bloodline.ui.extension.IntentFor
import mobi.cwiklinski.smog.R
import mobi.cwiklinski.smog.config.Constants
import mobi.cwiklinski.smog.database.AppContract
import mobi.cwiklinski.smog.ui.WidgetProvider
import java.util.*


class TimeService : Service() {

    val tickReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action.equals(Intent.ACTION_TIME_TICK)) {
                var minute = Calendar.getInstance().get(Calendar.MINUTE)
                if (minute == 0 || minute == 30) {
                    refreshReadings()
                }
            }
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(tickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(tickReceiver)
    }

    private fun refreshReadings() {
        ReadingService.refresh(this)
    }
}