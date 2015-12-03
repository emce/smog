package mobi.cwiklinski.smog.config

import java.text.SimpleDateFormat
import java.util.*


object Constants {
    public val TIMEOUT: Long = 30

    public val EXTRA_ACTION = "action"

    public val URL_READINGS = "http://powietrze.malopolska.pl/aqa/krakow.php"
    public val URL_BASE = "http://monitoring.krakow.pios.gov.pl"
    public val URL_ARCHIVE = "$URL_BASE/dane-pomiarowe/pobierz"

    public val LOADER_LATEST = 100
    public val LOADER_GRAPH = 101

    public val JSON_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    public val PM10_NORM = 50

    public val KEY_COOKIES = "cookies"

    enum class Action {
        FIRST,
        LATEST
    }

    enum class Place(val label: String) {
        KRASINSKIEGO("Al. Krasińskiego"),
        NOWA_HUTA("ul. Bulwarowa"),
        KURDWANOW("ul. Bujaka");

        companion object {
            fun byLabel(label: String): Place {
                when (label) {
                    KRASINSKIEGO.label -> return KRASINSKIEGO
                    NOWA_HUTA.label -> return NOWA_HUTA
                    KURDWANOW.label -> return KURDWANOW
                    else -> {
                        return KRASINSKIEGO
                    }
                }
            }
        }
    }
}