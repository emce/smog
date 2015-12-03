package mobi.cwiklinski.smog.database

import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import java.util.*

class AppProvider : ContentProvider() {

    var mOpenHelper: AppDatabase? = null;
    val mMatcher: AppUriMatcher = AppUriMatcher()
    var isBulk = false

    override fun onCreate(): Boolean {
        mOpenHelper = AppDatabase(context)
        return true;
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        var db: SQLiteDatabase = mOpenHelper!!.writableDatabase
        var match = mMatcher.matcher.match(uri)
        var syncToNetwork = !AppContract.hasCallerIsSyncAdapterParameter(uri!!)
        when (match) {
            AppUriMatcher.READINGS -> {
                var donationId = db.insertOrThrow(AppContract.Readings.TABLE_NAME, null, values);
                notifyChange(uri, syncToNetwork);
                return AppContract.Readings.buildReadingUri(donationId.toString());
            }
            else -> {
                throw UnsupportedOperationException("Unknown uri: " + uri)
            }
        }
    }

    override fun query(uri: Uri?, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        var db: SQLiteDatabase = mOpenHelper!!.readableDatabase
        var match = mMatcher.matcher.match(uri)
        if (uri != null) {
            if (match > 0) {
                var builder = buildSimpleSelection(uri)
                var cursor = builder.where(selection, selectionArgs)
                        .query(db, projection, sortOrder)
                cursor.setNotificationUri(context.contentResolver, uri)
                return cursor
            }
        }
        return null
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        var db: SQLiteDatabase = mOpenHelper!!.writableDatabase
        var builder = buildSimpleSelection(uri!!)
        var retVal = builder.where(selection, selectionArgs).update(db, values!!);
        var syncToNetwork: Boolean = !AppContract.hasCallerIsSyncAdapterParameter(uri);
        context.contentResolver.notifyChange(uri, null, syncToNetwork);
        return retVal
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uri!!.equals(AppContract.BASE_CONTENT_URI)) {
            deleteDatabase()
            context.contentResolver.notifyChange(uri, null, false);
            return 1;
        }
        var db: SQLiteDatabase = mOpenHelper!!.writableDatabase
        var builder = buildSimpleSelection(uri)
        var retVal = builder.where(selection, selectionArgs).delete(db)
        context.contentResolver.notifyChange(uri, null,
                !AppContract.hasCallerIsSyncAdapterParameter(uri))
        return retVal
    }

    override fun getType(uri: Uri?): String? {
        var match = mMatcher.matcher.match(uri)
        when (match) {
            AppUriMatcher.READINGS -> return AppContract.Readings.CONTENT_TYPE
            AppUriMatcher.READINGS_ID -> return AppContract.Readings.CONTENT_ITEM_TYPE
            else -> {
                throw UnsupportedOperationException("Unknown uri: " + uri)
            }
        }
    }

    fun buildSimpleSelection(uri: Uri) : SelectionBuilder {
        var builder = SelectionBuilder()
        var match = mMatcher.matcher.match(uri)
        when (match) {
            AppUriMatcher.READINGS -> return builder.table(AppContract.Readings.TABLE_NAME)
            AppUriMatcher.READINGS_ID -> {
                var donationId = AppContract.Readings.getReadingId(uri);
                return builder.table(AppContract.Readings.TABLE_NAME)
                        .where(AppContract.Readings._ID + "=?", arrayOf(donationId))
            }
            else -> {
                throw UnsupportedOperationException("Unknown uri: " + uri)
            }
        }
    }

    override fun applyBatch(operations: ArrayList<ContentProviderOperation>?): Array<out ContentProviderResult>? {
        var db: SQLiteDatabase = mOpenHelper!!.writableDatabase
        var results : Array<ContentProviderResult>?
        isBulk = true
        db.beginTransaction()
        try {
            var numOperations: Int = operations!!.size
            results = arrayOf<ContentProviderResult>()
            operations.forEach {
                it.apply(this, results, numOperations)
                numOperations--
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction()
        }
        return results
    }

    fun notifyChange(uri: Uri, syncToNetwork: Boolean) {
        if (!isBulk) {
            context.contentResolver.notifyChange(uri, null, syncToNetwork);
        }
    }

    fun deleteDatabase() {
        mOpenHelper!!.close()
        mOpenHelper!!.deleteDatabase(context);
        mOpenHelper = AppDatabase(context);
    }

}
