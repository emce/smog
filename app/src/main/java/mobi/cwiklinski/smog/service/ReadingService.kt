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
import mobi.cwiklinski.smog.network.PersistentCookieStore
import mobi.cwiklinski.smog.ui.WidgetProvider
import org.json.JSONArray
import java.net.*
import java.util.*
import java.util.concurrent.TimeUnit


class ReadingService : IntentService(ReadingService::class.java.simpleName) {

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
        var query: String = URLEncoder.encode("{'measType':'Auto','viewType':'Parameter','dateRange':'Day','date':'29.11.2015','viewTypeEntityId':'pm10','channels':[46,148,57]}", "utf-8")
        var headers = mapOf(
                Pair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"),
                Pair("Accept", "application/json, text/javascript, */*; q=0.01"),
                Pair("Origin", "http://monitoring.krakow.pios.gov.pl"),
                Pair("Accept-Language", "pl-PL,pl;q=0.8,en-US;q=0.6,en;q=0.4"),
                Pair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36"),
                Pair("Referer", "http://monitoring.krakow.pios.gov.pl/dane-pomiarowe/automatyczne"),
                Pair("X-Requested-With",  "XMLHttpRequest"),
                Pair("Connection", "keep-alive"),
                Pair("Cookie", "cookiesAccepted=yes; PHPSESSID=kpun7dv7e1hrgjgtspmv257o84; start_selector_nth=0; start_selector_hide=yes")
        )
        var getRequest = Request.Builder().url("http://monitoring.krakow.pios.gov.pl/dane-pomiarowe/automatyczne").build()
        getClient().newCall(getRequest).execute()

        var requestBuilder = Request.Builder().url(Constants.URL_ARCHIVE)
        headers.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        requestBuilder.post(RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"), "query=$query"))
        var response = getClient().newCall(requestBuilder.build()).execute()
        d(response!!.request().headers().toString())
        d(response.headers().toString())
        d(response.body()!!.string())
        // curl 'http://monitoring.krakow.pios.gov.pl/dane-pomiarowe/pobierz' -H 'Cookie: cookiesAccepted=yes; PHPSESSID=kpun7dv7e1hrgjgtspmv257o84; start_selector_nth=0; start_selector_hide=yes' -H 'Origin: http://monitoring.krakow.pios.gov.pl' -H 'Accept-Encoding: gzip, deflate' -H 'Accept-Language: pl-PL,pl;q=0.8,en-US;q=0.6,en;q=0.4' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36' -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' -H 'Accept: application/json, text/javascript, */*; q=0.01' -H 'Referer: http://monitoring.krakow.pios.gov.pl/dane-pomiarowe/automatyczne' -H 'X-Requested-With: XMLHttpRequest' -H 'Connection: keep-alive' --data 'query=%7B%22measType%22%3A%22Auto%22%2C%22viewType%22%3A%22Parameter%22%2C%22dateRange%22%3A%22Day%22%2C%22date%22%3A%2230.11.2015%22%2C%22viewTypeEntityId%22%3A%22pm10%22%2C%22channels%22%3A%5B46%2C148%2C57%5D%7D' --compressed
        // curl -i -X POST -d "query=%7B%27measType%27%3A%27Auto%27%2C%27viewType%27%3A%27Parameter%27%2C%27dateRange%27%3A%27Day%27%2C%27date%27%3A%2729.11.2015%27%2C%27viewTypeEntityId%27%3A%27pm10%27%2C%27channels%27%3A%5B46%2C148%2C57%5D%7D" -H "X-Requested-With:XMLHttpRequest" -H "Referer:http://monitoring.krakow.pios.gov.pl/dane-pomiarowe/automatyczne" -H "Content-Type:application/x-www-form-urlencoded; charset=UTF-8" -H "Origin:http://monitoring.krakow.pios.gov.pl" -H "User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36" -H "Accept:application/json, text/javascript, */*; q=0.01" -H "Connection:keep-alive" -H "Accept-Encoding:gzip, deflate" -H "Accept-Language:pl-PL,pl;q=0.8,en-US;q=0.6,en;q=0.4" "http://monitoring.krakow.pios.gov.pl/dane-pomiarowe/pobierz"

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