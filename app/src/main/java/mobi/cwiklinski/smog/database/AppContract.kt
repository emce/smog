package mobi.cwiklinski.smog.database

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.text.TextUtils

object AppContract {

    val AUTHORITY = "mobi.cwiklinski.smog"
    val BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY)
    val PATH_READINGS = "readings"

    public object Readings {
        val TABLE_NAME = "readings"
        val _ID = "_id"
        val DATE = "datetime"
        val YEAR = "year"
        val MONTH = "month"
        val DAY = "day"
        val HOUR = "hour"
        val AMOUNT = "amount"
        val PLACE = "place"
        val COLOR = "color"
        val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_READINGS).build()
        val CONTENT_TYPE = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/$AUTHORITY.$PATH_READINGS"
        val CONTENT_ITEM_TYPE = "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/$AUTHORITY.$PATH_READINGS"

        fun buildReadingUri(donationId: String) : Uri {
            return CONTENT_URI.buildUpon().appendPath(donationId).build()
        }

        fun getReadingId(uri: Uri) : String {
            return uri.pathSegments[1]
        }
    }

    fun hasCallerIsSyncAdapterParameter(uri: Uri): Boolean {
        return TextUtils.equals("true",
                uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER))
    }

    fun addCallerIsSyncAdapterParameter(uri: Uri) : Uri {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build()
    }
}