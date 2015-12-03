package mobi.cwiklinski.smog.network

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.RequestBody
import mobi.cwiklinski.bloodline.ui.extension.d
import java.net.CookieManager
import java.net.CookiePolicy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class OkClient : Client {

    val okHttpClient = OkHttpClient()
    val requestBuilder = com.squareup.okhttp.Request.Builder()

    override fun executeRequest(request: Request): Response {
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS)
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS)
        okHttpClient.followRedirects = true
        okHttpClient.setFollowSslRedirects(true)

        var cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        okHttpClient.setCookieHandler(cookieManager)

        okHttpClient.setSslSocketFactory(provideSSLContext().socketFactory)
        val response = Response()
        requestBuilder.url(request.path)
        when (request.httpMethod) {
            Method.POST -> requestBuilder.post(RequestBody.create(
                    MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"), request.httpBody))
        }
        request.httpHeaders.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        var okRequest = requestBuilder.build()
        d(okRequest.toString())
        var okResponse = okHttpClient.newCall(okRequest).execute();
        var body = okResponse.body()
        response.url = okResponse.request().url()
        response.httpStatusCode = okResponse.code()
        response.httpResponseMessage = okResponse.message()
        response.httpResponseHeaders = okResponse.headers().toMultimap()
        response.httpContentLength = body.contentLength()
        response.data = body.bytes()
        return response
    }

    private fun provideSSLContext(): SSLContext {
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(object: X509TrustManager {
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<out X509Certificate>? = null

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }
        }), SecureRandom())

        return sslContext
    }
}