package mobi.cwiklinski.smog.database

import android.database.Cursor


public data class Reading(
        val year: Int = 0,
        val month: Int = 0,
        val day: Int = 0,
        val hour: Int = 0,
        val place: Int = 0,
        val amount: Int = 0,
        val color: String? = null
) {

    companion object {
        fun fromCursor(cursor: Cursor): Reading {
            return Reading(
                    CursorHelper.getInt(cursor, AppContract.Readings.YEAR),
                    CursorHelper.getInt(cursor, AppContract.Readings.MONTH),
                    CursorHelper.getInt(cursor, AppContract.Readings.DAY),
                    CursorHelper.getInt(cursor, AppContract.Readings.HOUR),
                    CursorHelper.getInt(cursor, AppContract.Readings.PLACE),
                    CursorHelper.getInt(cursor, AppContract.Readings.AMOUNT),
                    CursorHelper.getString(cursor, AppContract.Readings.COLOR)
            )
        }
    }

}