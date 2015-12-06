package mobi.cwiklinski.smog.service

import android.app.IntentService
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.orhanobut.hawk.Hawk
import com.squareup.okhttp.*
import mobi.cwiklinski.bloodline.ui.extension.IntentFor
import mobi.cwiklinski.bloodline.ui.extension.d
import mobi.cwiklinski.bloodline.ui.extension.e
import mobi.cwiklinski.smog.config.Constants
import mobi.cwiklinski.smog.database.AppContract
import mobi.cwiklinski.smog.ui.WidgetProvider
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit


class ReadingService : IntentService(ReadingService::class.java.simpleName) {

    val MEDIA_TYPE_MARKDOWN = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")

    companion object {
        fun refresh(context: Context) {
            var runIntent = IntentFor<ReadingService>(context)
            var extras = Bundle()
            extras.putSerializable(Constants.EXTRA_ACTION, Constants.Action.LATEST)
            runIntent.putExtras(extras)
            context.startService(runIntent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent!!.getSerializableExtra(Constants.EXTRA_ACTION)) {
            Constants.Action.LATEST -> {
                //fetchLatest()
                fetchDay()
            }
            else -> {
                d("No action")
            }
        }
    }

    private fun fetchLatest() {
        val batch = ArrayList<ContentProviderOperation>()
        var response = URL(Constants.URL_READINGS).readText()
        if (!response.isEmpty()) {
            var body = response.removePrefix("(").removeSuffix(")")
            d(body!!)
            var json = JSONArray(body)
            if (json.length() > 0) {
                var values = json.optJSONArray(0)
                if (values!!.length() > 0) {
                    for (i: Int in 0..values.length()) {
                        var item = values.optJSONArray(i)
                        if (item != null && item.length() > 5) {
                            var label = item.optString(1)
                            var time = item.optString(2)
                            var type = item.optString(3)
                            var value = item.optInt(4, -1)
                            var color = item.optString(5)
                            if (type != null && type.equals("PM10") && time != null && value >= 0 && label != null) {
                                var date = Constants.JSON_DATE_FORMAT.parse(time)
                                var calendar = Calendar.getInstance()
                                calendar.time = date
                                batch.add(ContentProviderOperation
                                        .newInsert(AppContract.Readings.CONTENT_URI)
                                        .withValue(AppContract.Readings.AMOUNT, value)
                                        .withValue(AppContract.Readings.DATE, time)
                                        .withValue(AppContract.Readings.YEAR, calendar.get(Calendar.YEAR))
                                        .withValue(AppContract.Readings.YEAR, calendar.get(Calendar.YEAR))
                                        .withValue(AppContract.Readings.MONTH, calendar.get(Calendar.MONTH))
                                        .withValue(AppContract.Readings.DAY, calendar.get(Calendar.DAY_OF_MONTH))
                                        .withValue(AppContract.Readings.HOUR, calendar.get(Calendar.HOUR_OF_DAY))
                                        .withValue(AppContract.Readings.PLACE, Constants.Place.byLabel(label).ordinal)
                                        .withValue(AppContract.Readings.COLOR, color)
                                        .build())
                            }
                        }
                    }
                }
            }
        }
        if (batch.size > 0) {
            d("Adding ${batch.size} new items")
            contentResolver.applyBatch(AppContract.AUTHORITY, batch.toArrayList())
            contentResolver.notifyChange(AppContract.Readings.CONTENT_URI, null)
            WidgetProvider.refreshWidgets(this)
        }
    }

    private fun fetchDay() {
        val batch = ArrayList<ContentProviderOperation>()
        var query: String = "{\"measType\":\"Auto\",\"viewType\":\"Parameter\",\"dateRange\":\"Day\",\"date\":\"${Constants.REFRESH_DATE_FORMAT.format(Date())}\",\"viewTypeEntityId\":\"pm10\",\"channels\":[46,148,57]}"
        var headers = mapOf(
                Pair("Accept", "application/json, text/javascript, */*; q=0.01"),
                Pair("Origin", "http://monitoring.krakow.pios.gov.pl"),
                Pair("Accept-Language", "pl-PL,pl;q=0.8,en-US;q=0.6,en;q=0.4"),
                Pair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36"),
                Pair("Referer", "http://monitoring.krakow.pios.gov.pl/dane-pomiarowe/automatyczne"),
                Pair("X-Requested-With",  "XMLHttpRequest"),
                Pair("Connection", "keep-alive"),
                Pair("Cookie", "cookiesAccepted=yes; PHPSESSID=kpun7dv7e1hrgjgtspmv257o84; start_selector_nth=0; start_selector_hide=yes")
        )
        d(headers.toString())
        var requestBuilder = Request.Builder().url(Constants.URL_ARCHIVE)
        headers.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        requestBuilder.post(RequestBody.create(MEDIA_TYPE_MARKDOWN, "query=$query"))
        var response = getClient().newCall(requestBuilder.build()).execute()
        var json = JSONObject(response!!.body().string())
        var data = json.optJSONObject("data")!!.optJSONArray("series")
        (0..data!!.length()).forEach {
            var item = data.optJSONObject(it)
            if (item != null && item.has("label") && item.has("data")) {
                d(item.toString())
                var station = Constants.Place.byLabel(item!!.optString("label"))
                var records = item.optJSONArray("data")
                (0..records!!.length()).forEach {
                    var record = records.optJSONArray(it)
                    if (record != null && record.length() == 2) {
                        d(record.toString())
                        var calendar = DateTime((record[0] as String).toLong() * 1000L)
                        d(calendar.toString())
                        batch.add(ContentProviderOperation
                                .newInsert(AppContract.Readings.CONTENT_URI)
                                .withValue(AppContract.Readings.AMOUNT, record[1])
                                .withValue(AppContract.Readings.DATE, Constants.JSON_DATE_FORMAT.format(calendar.toDate()))
                                .withValue(AppContract.Readings.YEAR, calendar.year().get())
                                .withValue(AppContract.Readings.MONTH, calendar.monthOfYear().get())
                                .withValue(AppContract.Readings.DAY, calendar.dayOfMonth().get())
                                .withValue(AppContract.Readings.HOUR, calendar.hourOfDay().get())
                                .withValue(AppContract.Readings.PLACE, station.ordinal)
                                .build())
                    }
                }
            }
        }
        if (batch.size > 0) {
            d("Adding ${batch.size} new items")
            contentResolver.applyBatch(AppContract.AUTHORITY, batch.toArrayList())
            contentResolver.notifyChange(AppContract.Readings.CONTENT_URI, null)
            WidgetProvider.refreshWidgets(this)
        }
    }

    private fun getClient(): OkHttpClient {
        val okHttpClient = OkHttpClient()
        okHttpClient.interceptors().add(AddCookiesInterceptor())
        okHttpClient.interceptors().add(ReceivedCookiesInterceptor())
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS)
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS)
        okHttpClient.followRedirects = true
        okHttpClient.setFollowSslRedirects(true)
        return okHttpClient
    }

    class AddCookiesInterceptor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response? {
            var builder = chain!!.request().newBuilder()
            var data: HashSet<String> = Hawk.get(Constants.KEY_COOKIES, HashSet<String>())
            data.forEach {
                e(it)
                builder.addHeader("Cookie", it)
            }
            return chain.proceed(builder.build())
        }

    }

    class ReceivedCookiesInterceptor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response? {
            var originalResponse = chain!!.proceed(chain.request())

            if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                var cookies = HashSet<String>()

                originalResponse.headers("Set-Cookie").forEach {
                    cookies.add(it)
                    e(it)
                }

                Hawk.put(Constants.KEY_COOKIES, cookies)

            }

            return originalResponse
        }

    }

}