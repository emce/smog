package mobi.cwiklinski.smog.model

import android.content.res.Resources
import com.squareup.sqlbrite.BriteContentResolver
import com.squareup.sqlbrite.SqlBrite
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import mobi.cwiklinski.smog.App
import mobi.cwiklinski.smog.R
import mobi.cwiklinski.smog.config.Constants
import mobi.cwiklinski.smog.database.AppContract
import mobi.cwiklinski.smog.database.Reading
import mobi.cwiklinski.smog.ui.WidgetProvider
import org.joda.time.DateTime
import rx.subjects.BehaviorSubject
import java.util.*

public class MainViewModel {
    var sqlBrite = SqlBrite.create()
    var resolver: BriteContentResolver
    var resources: Resources
    var mainTime0Text = BehaviorSubject.create("")
    var mainTime1Text = BehaviorSubject.create("")
    var mainTime2Text = BehaviorSubject.create("")
    var mainTime3Text = BehaviorSubject.create("")
    var mainTime4Text = BehaviorSubject.create("")
    var mainAmount0Text = BehaviorSubject.create("")
    var mainAmount1Text = BehaviorSubject.create("")
    var mainAmount2Text = BehaviorSubject.create("")
    var mainAmount3Text = BehaviorSubject.create("")
    var mainAmount4Text = BehaviorSubject.create("")
    var progress0Max = BehaviorSubject.create(100)
    var progress1Max = BehaviorSubject.create(100)
    var progress2Max = BehaviorSubject.create(100)
    var progress3Max = BehaviorSubject.create(100)
    var progress4Max = BehaviorSubject.create(100)
    var progress0Value = BehaviorSubject.create(0)
    var progress1Value = BehaviorSubject.create(0)
    var progress2Value = BehaviorSubject.create(0)
    var progress3Value = BehaviorSubject.create(0)
    var progress4Value = BehaviorSubject.create(0)
    var progress0Color = BehaviorSubject.create(0)
    var progress1Color = BehaviorSubject.create(0)
    var progress2Color = BehaviorSubject.create(0)
    var progress3Color = BehaviorSubject.create(0)
    var progress4Color = BehaviorSubject.create(0)
    var listener: MainViewModelListener? = null

    init {
        resolver = sqlBrite.wrapContentProvider(App.instance.contentResolver)
        resources = App.instance.resources
    }

    public fun queryForData() {
        var date = DateTime()
        var latestQuery = resolver!!.createQuery(
            AppContract.Readings.CONTENT_URI, null,
            "${AppContract.Readings.YEAR}=? AND ${AppContract.Readings.MONTH}=? AND ${AppContract.Readings.DAY}=?",
            arrayOf(date.year().get().toString(), date.monthOfYear().get().toString(), date.dayOfMonth().get().toString()),
            "${AppContract.Readings.DAY} DESC, ${AppContract.Readings.HOUR} DESC LIMIT 3",
            false
        )
        var graphQuery = resolver!!.createQuery(
                AppContract.Readings.CONTENT_URI, null,
                "${AppContract.Readings.YEAR}=? AND ${AppContract.Readings.MONTH}=? AND ${AppContract.Readings.DAY}=?",
                arrayOf(date.year().get().toString(), date.monthOfYear().get().toString(), date.dayOfMonth().get().toString()),
                "${AppContract.Readings.DAY} DESC, ${AppContract.Readings.HOUR} ASC",
                false
        )
        latestQuery.subscribe({
            var data = it!!.run()
            if (data!!.count > 0) {
                var max = Constants.PM10_NORM
                var records = ArrayList<Reading>()
                while (data.moveToNext()) {
                    var readings = Reading.fromCursor(data)
                    records.add(readings)
                    max = Math.max(max, readings.amount)
                }
                max = ((max + 99) / 100 ) * 100
                records.forEach {
                    when (it.place) {
                        Constants.Place.KRASINSKIEGO.ordinal -> {
                            mainTime0Text.onNext("${it.hour}.00")
                            mainAmount0Text.onNext(getString(R.string.value_value).format(it.amount))
                            progress0Max.onNext(max)
                            progress0Value.onNext(it.amount)
                            progress0Color.onNext(Constants.Color.getResource(it.amount))
                        }
                        Constants.Place.NOWA_HUTA.ordinal -> {
                            mainTime1Text.onNext("${it.hour}.00")
                            mainAmount1Text.onNext(getString(R.string.value_value).format(it.amount))
                            progress1Max.onNext(max)
                            progress1Value.onNext(it.amount)
                            progress1Color.onNext(Constants.Color.getResource(it.amount))
                        }
                        Constants.Place.KURDWANOW.ordinal -> {
                            mainTime2Text.onNext("${it.hour}.00")
                            mainAmount2Text.onNext(getString(R.string.value_value).format(it.amount))
                            progress2Max.onNext(max)
                            progress2Value.onNext(it.amount)
                            progress2Color.onNext(Constants.Color.getResource(it.amount))
                        }
                        Constants.Place.DIETLA.ordinal -> {
                            mainTime3Text.onNext("${it.hour}.00")
                            mainAmount3Text.onNext(getString(R.string.value_value).format(it.amount))
                            progress3Max.onNext(max)
                            progress3Value.onNext(it.amount)
                            progress3Color.onNext(Constants.Color.getResource(it.amount))
                        }
                        Constants.Place.DIETLA.ordinal -> {
                            mainTime4Text.onNext("${it.hour}.00")
                            mainAmount4Text.onNext(getString(R.string.value_value).format(it.amount))
                            progress4Max.onNext(max)
                            progress4Value.onNext(it.amount)
                            progress4Color.onNext(Constants.Color.getResource(it.amount))
                        }
                    }
                }
                WidgetProvider.refreshWidgets(App.instance)
            }
        })
        graphQuery.subscribe({
            var data = it!!.run()
            if (data!!.count > 0) {
                var series0 = ArrayList<PointValue>()
                var series1 = ArrayList<PointValue>()
                var series2 = ArrayList<PointValue>()
                var series3 = ArrayList<PointValue>()
                var series4 = ArrayList<PointValue>()
                var labelsX = ArrayList<Float>()
                var labelsY = ArrayList<Float>()
                while (data!!.moveToNext()) {
                    var readings = Reading.fromCursor(data)
                    labelsX.add(readings.hour * 1f)
                    labelsY.add(readings.amount * 1f)
                    when (readings.place) {
                        Constants.Place.KRASINSKIEGO.ordinal -> {
                            series0.add(PointValue(readings.hour * 1f, readings.amount * 1f))
                        }
                        Constants.Place.NOWA_HUTA.ordinal -> {
                            series1.add(PointValue(readings.hour * 1f, readings.amount * 1f))
                        }
                        Constants.Place.KURDWANOW.ordinal -> {
                            series2.add(PointValue(readings.hour * 1f, readings.amount * 1f))
                        }
                        Constants.Place.DIETLA.ordinal -> {
                            series3.add(PointValue(readings.hour * 1f, readings.amount * 1f))
                        }
                        Constants.Place.BRONOWICE.ordinal -> {
                            series3.add(PointValue(readings.hour * 1f, readings.amount * 1f))
                        }
                    }
                }
                if (series0.size > 1) {
                    for(i: Int in 0..series0.size) {
                        if (i < series0.size && i < series1.size && i < series2.size) {
                            var value = (series0[i].y + series1[i].y + series2[i].y) / 3
                            series4.add(PointValue(series0[i].x, value))
                        }
                    }
                    var line0 = Line(series0).setColor(resources.getColor(R.color.color_0))
                            .setCubic(true).setHasPoints(false)
                    var line1 = Line(series1).setColor(resources.getColor(R.color.color_1))
                            .setCubic(true).setHasPoints(false)
                    var line2 = Line(series2).setColor(resources.getColor(R.color.color_2))
                            .setCubic(true).setHasPoints(false)
                    var line3 = Line(series3).setColor(resources.getColor(R.color.color_3))
                            .setCubic(true).setHasPoints(false)
                    var line4 = Line(series3).setColor(resources.getColor(R.color.color_4))
                            .setCubic(true).setHasPoints(false)
                    var line5 = Line(series4).setColor(resources.getColor(R.color.red))
                            .setCubic(true).setHasPoints(false).setStrokeWidth(5)
                    var lines = ArrayList<Line>()
                    lines.add(line0)
                    lines.add(line1)
                    lines.add(line2)
                    lines.add(line3)
                    lines.add(line4)
                    lines.add(line5)
                    var data = LineChartData().setLines(lines)
                    data.axisYLeft = Axis.generateAxisFromCollection(labelsY).setAutoGenerated(true)
                            .setHasTiltedLabels(true).setTextSize(resources.getDimensionPixelSize(R.dimen.text_pico))
                            .setName(getString(R.string.unit))
                    var formatter = SimpleAxisValueFormatter()
                    formatter.setAppendedText(":00".toCharArray())
                    data.axisXBottom = Axis.generateAxisFromCollection(labelsX).setAutoGenerated(true)
                            .setHasTiltedLabels(true).setTextSize(resources.getDimensionPixelSize(R.dimen.text_pico))
                            .setFormatter(formatter)
                    listener!!.onGraphDataLoaded(data, labelsY.min(), labelsY.max())
                }
            }
        })
    }

    private fun getString(resourceId: Int): String {
        return resources.getString(resourceId)
    }

    public interface MainViewModelListener {
        fun onGraphDataLoaded(chartData: LineChartData, min: Float?, max: Float?)
    }
}
