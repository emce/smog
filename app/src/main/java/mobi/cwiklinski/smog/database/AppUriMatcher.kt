package mobi.cwiklinski.smog.database

import android.content.UriMatcher
import android.text.TextUtils

class AppUriMatcher {

    public companion object {
        val PATH_SEPARATOR = "/";
        val NUMBER = "#";
        val WILDCARD = "*";
        val READINGS = 100;
        val READINGS_ID = 101;
    }
    var matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        addEndpoint(matcher, READINGS, AppContract.PATH_READINGS);
        addEndpoint(matcher, READINGS_ID, AppContract.PATH_READINGS, NUMBER);
    }

    fun addEndpoint(matcher: UriMatcher, code: Int, vararg pathSegments: String) {
        matcher.addURI(AppContract.AUTHORITY, TextUtils.join(PATH_SEPARATOR, pathSegments), code);
    }
}