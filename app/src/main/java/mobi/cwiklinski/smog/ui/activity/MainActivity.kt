package mobi.cwiklinski.smog.ui.activity

import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.TypiconsIcons
import kotlinx.android.synthetic.activity_main.*
import mobi.cwiklinski.bloodline.ui.extension.setToolbar
import mobi.cwiklinski.bloodline.ui.extension.startLoader
import mobi.cwiklinski.smog.R
import mobi.cwiklinski.smog.config.Constants
import mobi.cwiklinski.smog.database.AppContract
import mobi.cwiklinski.smog.database.Reading
import mobi.cwiklinski.smog.service.ReadingService
import mobi.cwiklinski.smog.ui.WidgetProvider
import org.eazegraph.lib.models.LegendModel
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries
import org.joda.time.DateTime
import java.util.*

class MainActivity : LoaderManager.LoaderCallbacks<Cursor>, AppCompatActivity() {

    var callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbar()
        ReadingService.refresh(this)
        FacebookSdk.sdkInitialize(applicationContext);
        mainChart.emptyDataText = getString(R.string.no_data)
    }

    override fun onResume() {
        super.onResume()
        startLoader(Constants.LOADER_LATEST, this)
        startLoader(Constants.LOADER_GRAPH, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        var refresh = menu!!.add(R.id.menu_group_main, R.id.menu_refresh, 2, R.string.app_name)
        MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_ALWAYS)
        refresh.setIcon(IconDrawable(this, TypiconsIcons.typcn_arrow_sync_outline).actionBarSize().color(Color.WHITE))
        var share = menu.add(R.id.menu_group_main, R.id.menu_share, 1, R.string.app_name)
        MenuItemCompat.setShowAsAction(share, MenuItemCompat.SHOW_AS_ACTION_ALWAYS)
        share.setIcon(IconDrawable(this, TypiconsIcons.typcn_export_outline).actionBarSize().color(Color.WHITE))
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_refresh -> {
                ReadingService.refresh(this)
                return true
            }
            R.id.menu_share -> {
                mainRoot.isDrawingCacheEnabled = true
                var photo = SharePhoto.Builder().setBitmap(mainRoot.drawingCache!!).build()
                var content = SharePhotoContent.Builder().addPhoto(photo).build()
                if (ShareDialog.canShow(SharePhotoContent::class.java)) {
                    ShareDialog(this).show(content)
                    return true
                }
                return false
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
        var loader = CursorLoader(this)
        var date = DateTime()
        when (id) {
            Constants.LOADER_LATEST -> {
                loader.uri = AppContract.Readings.CONTENT_URI
                loader.selection = "${AppContract.Readings.YEAR}=? AND ${AppContract.Readings.MONTH}=? AND ${AppContract.Readings.DAY}=?"
                loader.selectionArgs = arrayOf(date.year().get().toString(), date.monthOfYear().get().toString(), date.dayOfMonth().get().toString())
                loader.sortOrder =  "${AppContract.Readings.DAY} DESC, ${AppContract.Readings.HOUR} DESC LIMIT 3"
            }
            Constants.LOADER_GRAPH -> {
                loader.uri = AppContract.Readings.CONTENT_URI
                loader.selection = "${AppContract.Readings.YEAR}=? AND ${AppContract.Readings.MONTH}=? AND ${AppContract.Readings.DAY}=?"
                loader.selectionArgs = arrayOf(date.year().get().toString(), date.monthOfYear().get().toString(), date.dayOfMonth().get().toString())
                loader.sortOrder =  "${AppContract.Readings.DAY} DESC, ${AppContract.Readings.HOUR} ASC LIMIT 30"
            }
        }
        return loader
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        when (loader!!.id) {
            Constants.LOADER_LATEST -> {
                var max = Constants.PM10_NORM
                var records = ArrayList<Reading>()
                while (data!!.moveToNext()) {
                    var readings = Reading.fromCursor(data)
                    records.add(readings)
                    max = Math.max(max, readings.amount)
                }
                max = ((max + 99) / 100 ) * 100
                records.forEach {
                    when (it.place) {
                        Constants.Place.KRASINSKIEGO.ordinal -> {
                            mainTime0.text = "${it.hour}.00"
                            mainAmount0.text = getString(R.string.value_value).format(it.amount)
                            mainValue0.max = max
                            mainValue0.progress = it.amount
                        }
                        Constants.Place.NOWA_HUTA.ordinal -> {
                            mainTime1.text = "${it.hour}.00"
                            mainAmount1.text = getString(R.string.value_value).format(it.amount)
                            mainValue1.max = max
                            mainValue1.progress = it.amount
                        }
                        Constants.Place.KURDWANOW.ordinal -> {
                            mainTime2.text = "${it.hour}.00"
                            mainAmount2.text = getString(R.string.value_value).format(it.amount)
                            mainValue2.max = max
                            mainValue2.progress = it.amount
                        }
                    }
                }
                WidgetProvider.refreshWidgets(this)
            }
            Constants.LOADER_GRAPH -> {
                var series0 = ValueLineSeries()
                series0.color = resources.getColor(R.color.red)
                var series1 = ValueLineSeries()
                series1.color = resources.getColor(R.color.green)
                var series2 = ValueLineSeries()
                series2.color = resources.getColor(R.color.blue)
                while (data!!.moveToNext()) {
                    var readings = Reading.fromCursor(data)
                    when (readings.place) {
                        Constants.Place.KRASINSKIEGO.ordinal -> {
                            series0.addPoint(ValueLinePoint("${readings.hour}.00", readings.amount * 1f))
                        }
                        Constants.Place.NOWA_HUTA.ordinal -> {
                            series1.addPoint(ValueLinePoint("${readings.hour}.00", readings.amount * 1f))
                        }
                        Constants.Place.KURDWANOW.ordinal -> {
                            series2.addPoint(ValueLinePoint("${readings.hour}.00", readings.amount * 1f))
                        }
                    }
                }
                if (series0.series.size > 1) {
                    var legends = listOf(
                        LegendModel(getString(R.string.station_0)),
                        LegendModel(getString(R.string.station_1)),
                        LegendModel(getString(R.string.station_2))
                    )
                    mainChart.addLegend(legends)
                    mainChart.addSeries(series0)
                    mainChart.addSeries(series1)
                    mainChart.addSeries(series2)
                    mainChart.startAnimation()
                }
            }
        }
    }

    private fun setProgressColor(color: String?, progress: ProgressBar) {
        if (color != null && !color!!.isEmpty()) {
            if (progress.progressDrawable!! is LayerDrawable) {
                var layerDrawable = progress.progressDrawable as LayerDrawable;
                var progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
                progressDrawable.setColorFilter(Color.parseColor("#$color"), PorterDuff.Mode.SRC_IN)
            }
        }
    }
}