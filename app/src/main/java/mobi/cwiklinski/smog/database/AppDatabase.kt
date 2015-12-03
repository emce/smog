package mobi.cwiklinski.smog.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase : SQLiteOpenHelper {

    public companion object {
        public val DATABASE_NAME = "smog.sqlite"
        public val DATABASE_VERSION = 1
    }

    constructor(context: Context) : super(context, DATABASE_NAME, null, DATABASE_VERSION)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SqlCommand.CREATE_TABLE_READINGS)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        var sync: Boolean = false
        var currentVersion = oldVersion
        if (sync) {
            //SynchronizationService.startFullSynchronization(mContext)
        }

        if (currentVersion != newVersion) {
            throw IllegalStateException("error upgrading the database to version "
                    + newVersion)
        }
    }

    fun deleteDatabase(context: Context) {
        context.deleteDatabase(DATABASE_NAME)
    }

    object SqlCommand {
        val CREATE_TABLE_READINGS = "CREATE TABLE ${AppContract.Readings.TABLE_NAME} (" +
            "${AppContract.Readings._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "${AppContract.Readings.DATE} VARCHAR NOT NULL, " +
            "${AppContract.Readings.YEAR} INTEGER NOT NULL, " +
            "${AppContract.Readings.MONTH} INTEGER NOT NULL, " +
            "${AppContract.Readings.DAY} INTEGER NOT NULL, " +
            "${AppContract.Readings.HOUR} INTEGER NOT NULL, " +
            "${AppContract.Readings.AMOUNT} INTEGER NOT NULL, " +
            "${AppContract.Readings.PLACE} INTEGER NOT NULL, " +
            "${AppContract.Readings.COLOR} VARCHAR, " +
            " UNIQUE (${AppContract.Readings.YEAR}, ${AppContract.Readings.MONTH}, " +
            "${AppContract.Readings.DAY}, ${AppContract.Readings.HOUR}, ${AppContract.Readings.PLACE}) ON CONFLICT REPLACE" +
            ")"
    }

}
