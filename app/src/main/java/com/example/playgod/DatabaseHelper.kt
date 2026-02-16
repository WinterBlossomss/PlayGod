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
private const val NOTE_COLUMN_NOTEFK = "NoteNoteFK"

// Tags table
private const val TAG_TABLE_NAME = "Tags"
private const val TAG_COLUMN_ID = "TagIDPK"
private const val TAG_COLUMN_TITLE = "TagName"
private const val TAG_COLUMN_CATFK = "TagCatFK"

// Categories table
private const val CAT_TABLE_NAME = "Categories"
private const val CAT_COLUMN_ID = "CatIDPK"
private const val CAT_COLUMN_TITLE = "CatName"

class DataBaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        val createCatsTable = """
            CREATE TABLE $CAT_TABLE_NAME (
                $CAT_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $CAT_COLUMN_TITLE TEXT NOT NULL
            )
        """.trimIndent()

        val createTagsTable = """
            CREATE TABLE $TAG_TABLE_NAME (
                $TAG_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TAG_COLUMN_TITLE TEXT NOT NULL,
                $TAG_COLUMN_CATFK INTEGER,
                FOREIGN KEY ($TAG_COLUMN_CATFK)
                    REFERENCES $CAT_TABLE_NAME($CAT_COLUMN_ID)
                    ON DELETE SET NULL
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
        db.execSQL(createCatsTable)
        db.execSQL(createTagsTable)
        db.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $NOTE_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TAG_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $CAT_TABLE_NAME")
        onCreate(db)
    }
    // CATEGORY FUNCTIONS
    fun addCat(catName: String): Long {
        val values = ContentValues().apply {
            put(CAT_COLUMN_TITLE, catName)
        }
        return writableDatabase.insert(CAT_TABLE_NAME, null, values)
    }
    fun getAllCats(): List<Pair<Int, String>> {
        val result = mutableListOf<Pair<Int, String>>()

        readableDatabase.query(
            CAT_TABLE_NAME,
            arrayOf(CAT_COLUMN_ID, CAT_COLUMN_TITLE),
            null, null, null, null,
            "$CAT_COLUMN_TITLE ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(CAT_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(CAT_COLUMN_TITLE)

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
    fun deleteCat(catId: Int): Int
    {
        return writableDatabase.delete(
            CAT_TABLE_NAME,
            "$CAT_COLUMN_ID = ?",
            arrayOf(catId.toString())
        )
    }
    fun createDefaultCats()
    {
        addCat("Geography & Locations")
        addCat("People & Groups")
        addCat("Culture & Society")
        addCat("History & Structure")
        addCat("Politics & Power")
        addCat("Magic & Technology")
    }

    // TAG FUNCTIONS

    fun addTag(tagName: String, catId: Int?): Long {
        val values = ContentValues().apply {
            put(TAG_COLUMN_TITLE, tagName)
            if (catId != null)
                put(TAG_COLUMN_CATFK, catId)
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

    fun deleteTag(catId: Int): Int
    {
        return writableDatabase.delete(
            TAG_TABLE_NAME,
            "$TAG_COLUMN_ID = ?",
            arrayOf(catId.toString())
        )
    }

    // INPUT DEFAULT TAGS
    fun addDefaultTags()
    {
        addTag("Continents",1)
        addTag("Countries / Realms",1)
        addTag("Regions",1)
        addTag("Cities",1)
        addTag("Towns",1)
        addTag("Villages",1)
        addTag("Landmarks",1)
        addTag("Rivers",1)
        addTag("Lakes",1)
        addTag("Seas / Oceans",1)
        addTag("Mountain Ranges",1)
        addTag("Forests",1)
        addTag("Deserts",1)
        addTag("Islands",1)
        addTag("Biomes",1)

        addTag("Characters",2)
        addTag("Historical Figures",2)
        addTag("Rulers",2)
        addTag("Heroes / Legends",2)
        addTag("Prominent Families",2)
        addTag("Dynasties",2)
        addTag("Noble Houses",2)
        addTag("Organizations",2)
        addTag("Factions",2)
        addTag("Guilds",2)
        addTag("Orders",2)

        addTag("Cultures",3)
        addTag("Ethnicities",3)
        addTag("Species / Races",3)
        addTag("Religions",3)
        addTag("Deities",3)
        addTag("Languages",3)
        addTag("Traditions",3)
        addTag("Customs",3)
        addTag("Legal Systems",3)

        addTag("Timeline",4)
        addTag("Historical Events",4)
        addTag("Wars",4)
        addTag("Treaties",4)
        addTag("Revolutions",4)
        addTag("Eras",4)

        addTag("Kingdoms",5)
        addTag("Empires",5)
        addTag("Republics",5)
        addTag("Political Systems",5)
        addTag("Alliances",5)
        addTag("Conflicts",5)

        addTag("Magic Systems",6)
        addTag("Creatures",6)
        addTag("Artifacts",6)
        addTag("Technologies",6)
        addTag("Prophecies",6)
        addTag("Myths / Legends",6)
    }


    // NOTE FUNCTIONS

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
