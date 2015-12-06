package mobi.cwiklinski.smog.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import mobi.cwiklinski.bloodline.ui.extension.IntentFor
import mobi.cwiklinski.smog.R
import mobi.cwiklinski.smog.config.Constants
import mobi.cwiklinski.smog.database.AppContract
import mobi.cwiklinski.smog.database.Reading
import mobi.cwiklinski.smog.ui.activity.MainActivity
import org.joda.time.DateTime
import java.util.*


class WidgetProvider : AppWidgetProvider() {

    companion object {
        public fun refreshWidgets(context: Context) {
            var remoteViews = RemoteViews(AppContract.AUTHORITY,
                    R.layout.widget_layout);
            var widget = ComponentName(context, WidgetProvider::class.java)
            var manager = AppWidgetManager.getInstance(context)
            fillWidgetViews(context, remoteViews)
            manager.updateAppWidget(widget, remoteViews)
        }

        public fun fillWidgetViews(context: Context, remoteViews: RemoteViews) {
            var date = DateTime()
            var sortOrder =  "${AppContract.Readings.YEAR} DESC, ${AppContract.Readings.MONTH} DESC,"+
                    " ${AppContract.Readings.DAY} DESC, ${AppContract.Readings.HOUR} DESC LIMIT 3"
            var selection = "${AppContract.Readings.YEAR}=? AND ${AppContract.Readings.MONTH}=? AND ${AppContract.Readings.DAY}=?"
            var selectionArgs = arrayOf(date.year().get().toString(), date.monthOfYear().get().toString(), date.dayOfMonth().get().toString())
            var data = context.contentResolver.query(AppContract.Readings.CONTENT_URI, null, selection, selectionArgs, sortOrder)
            try {
                var records = ArrayList<Reading>()
                while (data.moveToNext()) {
                    records.add(Reading.fromCursor(data))
                }
                var sum = 0
                records.forEach {
                    when (it.place) {
                        Constants.Place.KRASINSKIEGO.ordinal -> {
                            remoteViews.setTextViewText(R.id.widgetTime0, "${it.hour}.00")
                            remoteViews.setTextViewText(R.id.widgetValue0,
                                    context.getString(R.string.value_value).format(it.amount))
                            sum += it.amount
                        }
                        Constants.Place.NOWA_HUTA.ordinal -> {
                            remoteViews.setTextViewText(R.id.widgetTime1, "${it.hour}.00")
                            remoteViews.setTextViewText(R.id.widgetValue1,
                                    context.getString(R.string.value_value).format(it.amount))
                            sum += it.amount
                        }
                        Constants.Place.KURDWANOW.ordinal -> {
                            remoteViews.setTextViewText(R.id.widgetTime2, "${it.hour}.00")
                            remoteViews.setTextViewText(R.id.widgetValue2,
                                    context.getString(R.string.value_value).format(it.amount))
                            sum += it.amount
                        }
                    }
                }
                when (sum / 3) {
                    0 -> remoteViews.setImageViewResource(R.id.widgetImage, R.drawable.ic_bus_yellow)
                    in (1 .. 149) -> remoteViews.setImageViewResource(R.id.widgetImage, R.drawable.ic_bus_red)
                    else -> remoteViews.setImageViewResource(R.id.widgetImage, R.drawable.ic_bus_green)
                }
                remoteViews.setOnClickPendingIntent(R.id.widgetRoot, getPendingIntent(context))
            } finally {
                data!!.close()
            }
            var iceWidget = ComponentName(context, WidgetProvider::class.java)
            var manager = AppWidgetManager.getInstance(context)
            manager.updateAppWidget(iceWidget, remoteViews)
        }

        public fun getPendingIntent(context: Context): PendingIntent {
            var intent = IntentFor<MainActivity>(context)
            return PendingIntent.getActivity(context, 0, intent, 0)
        }
    }


    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds!!.forEach {
            drawWidget(context!!, it)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        drawWidget(context!!, appWidgetId)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        redrawWidgets(context!!)
    }

    private fun drawWidget(context: Context, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            checkWidgetTypeAndGetView(context, id);
        } else {
            var remoteViews = RemoteViews(context.packageName,
                    R.layout.widget_layout)
            fillWidgetViews(context, remoteViews)
        }
    }

    private fun redrawWidgets(context: Context) {
        var appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WidgetProvider::class.java));
        appWidgetIds!!.forEach {
            drawWidget(context, it)
        }
    }

    private fun checkWidgetTypeAndGetView(context: Context, id: Int) {
        var appWidgetManager = AppWidgetManager.getInstance(context)
        var widgetOptions = appWidgetManager.getAppWidgetOptions(id)
        var category = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1)
        var isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD

        if (!isKeyguard) {
            var remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
            fillWidgetViews(context, remoteViews)
        }
    }
}