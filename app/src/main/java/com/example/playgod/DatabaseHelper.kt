package com.example.playgod

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "Android"

// Notes table
private const val NOTE_TABLE_NAME = "Notes"
private const val NOTE_COLUMN_ID = "NoteIDPK"
private const val NOTE_COLUMN_TITLE = "NoteName"
private const val NOTE_COLUMN_CONTENT = "NoteContent"
private const val NOTE_COLUMN_TAGFK = "NoteTagFK"

// Tags table
private const val TAG_TABLE_NAME = "Tags"
private const val TAG_COLUMN_ID = "TagIDPK"
private const val TAG_COLUMN_TITLE = "TagName"

class DataBaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        // Create Tags table first (because Notes references it)
        val createTagsTable = """
            CREATE TABLE $TAG_TABLE_NAME (
                $TAG_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TAG_COLUMN_TITLE TEXT NOT NULL
            )
        """.trimIndent()

        val createNotesTable = """
            CREATE TABLE $NOTE_TABLE_NAME (
                $NOTE_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $NOTE_COLUMN_TITLE TEXT NOT NULL,
                $NOTE_COLUMN_CONTENT TEXT NOT NULL,
                $NOTE_COLUMN_TAGFK INTEGER,
                FOREIGN KEY ($NOTE_COLUMN_TAGFK)
                    REFERENCES $TAG_TABLE_NAME($TAG_COLUMN_ID)
                    ON DELETE SET NULL
            )
        """.trimIndent()

        db.execSQL(createTagsTable)
        db.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $NOTE_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TAG_TABLE_NAME")
        onCreate(db)
    }

    // -------------------------
    // TAG FUNCTIONS
    // -------------------------

    fun addTag(tagName: String): Long {
        val values = ContentValues().apply {
            put(TAG_COLUMN_TITLE, tagName)
        }
        return writableDatabase.insert(TAG_TABLE_NAME, null, values)
    }

    fun getAllTags(): List<Pair<Int, String>> {
        val result = mutableListOf<Pair<Int, String>>()

        readableDatabase.query(
            TAG_TABLE_NAME,
            arrayOf(TAG_COLUMN_ID, TAG_COLUMN_TITLE),
            null, null, null, null,
            "$TAG_COLUMN_TITLE ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(TAG_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(TAG_COLUMN_TITLE)

            while (c.moveToNext()) {
                result.add(
                    Pair(
                        c.getInt(idIx),
                        c.getString(titleIx)
                    )
                )
            }
        }
        return result
    }

    // -------------------------
    // NOTE FUNCTIONS
    // -------------------------

    fun addNote(title: String, content: String, tagId: Int?): Long {
        val values = ContentValues().apply {
            put(NOTE_COLUMN_TITLE, title)
            put(NOTE_COLUMN_CONTENT, content)
            if (tagId != null)
                put(NOTE_COLUMN_TAGFK, tagId)
        }

        return writableDatabase.insert(NOTE_TABLE_NAME, null, values)
    }

    fun getAllNotes(): List<Triple<Int, String, String>> {
        val result = mutableListOf<Triple<Int, String, String>>()

        readableDatabase.query(
            NOTE_TABLE_NAME,
            arrayOf(NOTE_COLUMN_ID, NOTE_COLUMN_TITLE, NOTE_COLUMN_CONTENT),
            null, null, null, null,
            "$NOTE_COLUMN_ID ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(NOTE_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TITLE)
            val contentIx = c.getColumnIndexOrThrow(NOTE_COLUMN_CONTENT)

            while (c.moveToNext()) {
                result.add(
                    Triple(
                        c.getInt(idIx),
                        c.getString(titleIx),
                        c.getString(contentIx)
                    )
                )
            }
        }

        return result
    }

    fun deleteNote(noteId: Int): Int {
        return writableDatabase.delete(
            NOTE_TABLE_NAME,
            "$NOTE_COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )
    }

    fun updateNote(noteId: Int, title: String, content: String, tagId: Int?): Int {
        val values = ContentValues().apply {
            put(NOTE_COLUMN_TITLE, title)
            put(NOTE_COLUMN_CONTENT, content)
            if (tagId != null)
                put(NOTE_COLUMN_TAGFK, tagId)
            else
                putNull(NOTE_COLUMN_TAGFK)
        }

        return writableDatabase.update(
            NOTE_TABLE_NAME,
            values,
            "$NOTE_COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )
    }
}
