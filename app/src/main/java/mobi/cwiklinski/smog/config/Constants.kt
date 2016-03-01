package mobi.cwiklinski.smog.config

import mobi.cwiklinski.smog.R
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

    public val REFRESH_DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    public val PM10_NORM = 50

    public val KEY_COOKIES = "cookies"

    enum class Action {
        FIRST,
        LATEST
    }

    enum class Place(val label: String) {
        KRASINSKIEGO("Aleja Krasińskiego"),
        NOWA_HUTA("Nowa Huta"),
        KURDWANOW("Kraków-Kurdwanów"),
        DIETLA("Kraków-ul. Dietla"),
        BRONOWICE("Kraków, ul. Złoty Róg");

        companion object {
            fun byLabel(label: String): Place {
                when (label) {
                    KRASINSKIEGO.label -> return KRASINSKIEGO
                    NOWA_HUTA.label -> return NOWA_HUTA
                    KURDWANOW.label -> return KURDWANOW
                    DIETLA.label -> return DIETLA
                    BRONOWICE.label -> return BRONOWICE
                    else -> {
                        return KRASINSKIEGO
                    }
                }
            }
        }
    }

    enum class Color(val level: Int, val colorRes: Int) {
        VERY_GOOD(30, R.color.green),
        GOOD(50, R.color.yellow),
        MODERATE(100, R.color.orange),
        SUFFICIENT(200, R.color.red),
        BAD(300, R.color.purple),
        VERY_BAD(500, R.color.violet);

        companion object {
            fun getResource(level: Int): Int {
                when (level) {
                    in 0..VERY_GOOD.level -> return VERY_GOOD.colorRes
                    in VERY_GOOD.level..GOOD.level -> return GOOD.colorRes
                    in GOOD.level..MODERATE.level -> return MODERATE.colorRes
                    in MODERATE.level..SUFFICIENT.level -> return SUFFICIENT.colorRes
                    in SUFFICIENT.level..BAD.level -> return BAD.colorRes
                    else -> return VERY_BAD.colorRes
                }
            }
        }
    }
}