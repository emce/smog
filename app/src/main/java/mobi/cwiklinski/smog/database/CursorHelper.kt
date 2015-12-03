package mobi.cwiklinski.smog.database

import android.database.Cursor

object CursorHelper {

    fun getLong(cursor: Cursor, columnName: String): Long {
        return cursor.getLong(cursor.getColumnIndexOrThrow(columnName))
    }

    fun getFloat(cursor: Cursor, columnName: String): Float {
        return cursor.getFloat(cursor.getColumnIndexOrThrow(columnName))
    }

    fun getInt(cursor: Cursor, columnName: String): Int {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName))
    }

    fun getString(cursor: Cursor, columnName: String): String? {
        val columnIndex = cursor.getColumnIndexOrThrow(columnName)
        if (cursor.isNull(columnIndex)) {
            return null
        } else {
            return cursor.getString(cursor.getColumnIndexOrThrow(columnName))
        }
    }

    fun getString(cursor: Cursor, columnName: String, alternate: String): String {
        val columnIndex = cursor.getColumnIndexOrThrow(columnName)
        if (cursor.isNull(columnIndex)) {
            return alternate
        } else {
            return cursor.getString(cursor.getColumnIndexOrThrow(columnName))
        }
    }

    fun getBoolean(cursor: Cursor, columnName: String): Boolean {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName)) == 1
    }

    fun getDouble(cursor: Cursor, columnName: String): Double? {
        val columnIndex = cursor.getColumnIndexOrThrow(columnName)
        if (cursor.isNull(columnIndex)) {
            return null
        } else {
            return cursor.getDouble(columnIndex)
        }
    }

    fun isNull(cursor: Cursor, columnName: String): Boolean {
        return cursor.isNull(cursor.getColumnIndexOrThrow(columnName))
    }


    fun isEmpty(cursor: Cursor?): Boolean {
        return cursor == null || cursor.count == 0
    }

    fun sumIntColumn(cursor: Cursor?, columnName: String): Int {
        var value = 0
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToPrevious()
            val columnIndex = cursor.getColumnIndexOrThrow(columnName)
            while (cursor.moveToNext()) {
                value += cursor.getInt(columnIndex)
            }
        }
        return value
    }

    fun sumDoubleColumn(cursor: Cursor?, columnName: String): Double {
        var value = 0.0
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToPrevious()
            val columnIndex = cursor.getColumnIndexOrThrow(columnName)
            while (cursor.moveToNext()) {
                value += cursor.getDouble(columnIndex)
            }
        }
        return value
    }
}