package mobi.cwiklinski.smog.network

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log

import java.io.*
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieStore(context: Context) : CookieStore {

    private val cookies: HashMap<String, ConcurrentHashMap<String, HttpCookie>>
    private val cookiePrefs: SharedPreferences

    init {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0)
        cookies = HashMap<String, ConcurrentHashMap<String, HttpCookie>>()

        // Load any previously stored cookies into the store
        val prefsMap = cookiePrefs.all
        for (entry in prefsMap.entries) {
            if (!(entry.value as String).startsWith(COOKIE_NAME_PREFIX)) {
                val cookieNames = TextUtils.split(entry.value as String, ",")
                for (name in cookieNames) {
                    val encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null)
                    if (encodedCookie != null) {
                        val decodedCookie = decodeCookie(encodedCookie)
                        if (decodedCookie != null) {
                            if (!cookies.containsKey(entry.key))
                                cookies.put(entry.key, ConcurrentHashMap<String, HttpCookie>())
                            cookies[entry.key]?.put(name, decodedCookie)
                        }
                    }
                }

            }
        }
    }

    override fun add(uri: URI, cookie: HttpCookie) {
        val name = getCookieToken(uri, cookie)

        // Save cookie into local store, or remove if expired
        if (!cookie.hasExpired()) {
            if (!cookies.containsKey(uri.host))
                cookies.put(uri.host, ConcurrentHashMap<String, HttpCookie>())
            cookies[uri.host]?.put(name, cookie)
        } else {
            if (cookies.containsKey(uri.toString()))
                cookies[uri.host]?.remove(name)
        }

        // Save cookie into persistent store
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.putString(uri.host, TextUtils.join(",", cookies[uri.host]?.keys))
        prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(SerializableHttpCookie(cookie)))
        prefsWriter.commit()
    }

    protected fun getCookieToken(uri: URI, cookie: HttpCookie): String {
        return cookie.name + cookie.domain
    }

    override fun get(uri: URI): List<HttpCookie> {
        val ret = ArrayList<HttpCookie>()
        if (cookies.containsKey(uri.host))
            ret.addAll(cookies[uri.host]!!.values)
        return ret
    }

    override fun removeAll(): Boolean {
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.commit()
        cookies.clear()
        return true
    }


    override fun remove(uri: URI, cookie: HttpCookie): Boolean {
        val name = getCookieToken(uri, cookie)

        if (cookies.containsKey(uri.host) && cookies[uri.host]!!.containsKey(name)) {
            cookies[uri.host]!!.remove(name)

            val prefsWriter = cookiePrefs.edit()
            if (cookiePrefs.contains(COOKIE_NAME_PREFIX + name)) {
                prefsWriter.remove(COOKIE_NAME_PREFIX + name)
            }
            prefsWriter.putString(uri.host, TextUtils.join(",", cookies[uri.host]!!.keys))
            prefsWriter.commit()

            return true
        } else {
            return false
        }
    }

    override fun getCookies(): List<HttpCookie> {
        val ret = ArrayList<HttpCookie>()
        for (key in cookies.keys)
            ret.addAll(cookies[key]!!.values)

        return ret
    }

    override fun getURIs(): List<URI> {
        val ret = ArrayList<URI>()
        for (key in cookies.keys)
            try {
                ret.add(URI(key))
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

        return ret
    }

    /**
     * Serializes Cookie object into String

     * @param cookie cookie to be encoded, can be null
     * *
     * @return cookie encoded as String
     */
    protected fun encodeCookie(cookie: SerializableHttpCookie?): String? {
        if (cookie == null)
            return null
        val os = ByteArrayOutputStream()
        try {
            val outputStream = ObjectOutputStream(os)
            outputStream.writeObject(cookie)
        } catch (e: IOException) {
            Log.d(LOG_TAG, "IOException in encodeCookie", e)
            return null
        }

        return byteArrayToHexString(os.toByteArray())
    }

    /**
     * Returns cookie decoded from cookie string

     * @param cookieString string of cookie as returned from http request
     * *
     * @return decoded cookie or null if exception occured
     */
    protected fun decodeCookie(cookieString: String): HttpCookie? {
        val bytes = hexStringToByteArray(cookieString)
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        var cookie: HttpCookie? = null
        try {
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as SerializableHttpCookie).getCookie()
        } catch (e: IOException) {
            Log.d(LOG_TAG, "IOException in decodeCookie", e)
        } catch (e: ClassNotFoundException) {
            Log.d(LOG_TAG, "ClassNotFoundException in decodeCookie", e)
        }

        return cookie
    }

    protected fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size() * 2)
        for (element in bytes) {
            val v = (element).toInt() and 0xff
            if (v < 16) {
                sb.append('0')
            }
            sb.append(Integer.toHexString(v))
        }
        return sb.toString().toUpperCase(Locale.US)
    }

    protected fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    companion object {

        private val LOG_TAG = "PersistentCookieStore"
        private val COOKIE_PREFS = "CookiePrefsFile"
        private val COOKIE_NAME_PREFIX = "cookie_"
    }
}