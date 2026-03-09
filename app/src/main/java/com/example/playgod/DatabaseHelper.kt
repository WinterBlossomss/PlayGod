package com.example.playgod

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "Android"
// Worlds table
private const val WORLD_TABLE_NAME = "Worlds"
private const val WORLD_COLUMN_ID = "WorldIDPK"
private const val WORLD_COLUMN_NAME = "WorldName"

// Notes table
private const val NOTE_TABLE_NAME = "Notes"
private const val NOTE_COLUMN_ID = "NoteIDPK"
private const val NOTE_COLUMN_TITLE = "NoteName"
private const val NOTE_COLUMN_CONTENT = "NoteContent"
private const val NOTE_COLUMN_BRFDESCR = "NoteBrfDescr"
private const val NOTE_COLUMN_TAGFK = "NoteTagFK"
private const val NOTE_COLUMN_WORLDFK = "NoteWorldFK"

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
        //Create strong tables first
        val createCatsTable = """
            CREATE TABLE $CAT_TABLE_NAME (
                $CAT_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $CAT_COLUMN_TITLE TEXT NOT NULL
            )
        """.trimIndent()
        val createWorldsTable = """
        CREATE TABLE $WORLD_TABLE_NAME (
        $WORLD_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $WORLD_COLUMN_NAME TEXT NOT NULL
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
                $NOTE_COLUMN_BRFDESCR TEXT NOT NULL,
                $NOTE_COLUMN_TAGFK INTEGER,
                $NOTE_COLUMN_WORLDFK INTEGER,
                FOREIGN KEY ($NOTE_COLUMN_TAGFK)
                    REFERENCES $TAG_TABLE_NAME($TAG_COLUMN_ID)
                    ON DELETE SET NULL,
                FOREIGN KEY ($NOTE_COLUMN_WORLDFK)
                    REFERENCES $WORLD_TABLE_NAME($WORLD_COLUMN_ID)
                    ON DELETE SET NULL
            )
        """.trimIndent()
        db.execSQL(createWorldsTable)
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
    // WORLD FUNCTIONS
    fun addWorld(worldName: String): Long {
        val values = ContentValues().apply {
            put(WORLD_COLUMN_NAME, worldName)
        }
        return writableDatabase.insert(WORLD_TABLE_NAME, null, values)
    }
    fun getAllWorlds(): List<Worlds> {
        val result = mutableListOf<Worlds>()

        readableDatabase.query(
            WORLD_TABLE_NAME,
            arrayOf(WORLD_COLUMN_ID, WORLD_COLUMN_NAME),
            null, null, null, null,
            "$WORLD_COLUMN_NAME ASC"
        ).use { c ->
            val idIx = c.getColumnIndexOrThrow(WORLD_COLUMN_ID)
            val nameIx = c.getColumnIndexOrThrow(WORLD_COLUMN_NAME)

            while (c.moveToNext()) {
                val world = Worlds().apply {
                    worldIDPK = c.getInt(idIx)
                    worldName = c.getString(nameIx)
                }
                result.add(world)
            }
        }
        return result
    }
    fun deleteWorld(worldId: Int): Int {
        return writableDatabase.delete(
            WORLD_TABLE_NAME,
            "$WORLD_COLUMN_ID = ?",
            arrayOf(worldId.toString())
        )
    }
    // CATEGORY FUNCTIONS
    fun addCat(catName: String): Long {
        val values = ContentValues().apply {
            put(CAT_COLUMN_TITLE, catName)
        }
        return writableDatabase.insert(CAT_TABLE_NAME, null, values)
    }
    fun getAllCats(): List<Categories> {
        val result = mutableListOf<Categories>()

        readableDatabase.query(
            CAT_TABLE_NAME,
            arrayOf(CAT_COLUMN_ID, CAT_COLUMN_TITLE),
            null, null, null, null,
            "$CAT_COLUMN_TITLE ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(CAT_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(CAT_COLUMN_TITLE)

            while (c.moveToNext()) {
                val cat = Categories().apply{
                    catIDPK = c.getInt(idIx)
                    catName = c.getString(titleIx)
                }
                result.add(cat)
            }
        }
        return result
    }
    fun deleteCat(catId: Int): Int {
        return writableDatabase.delete(
            CAT_TABLE_NAME,
            "$CAT_COLUMN_ID = ?",
            arrayOf(catId.toString())
        )
    }
    fun createDefaultCats() {
        if(getCatCount() == 0)
        {
            addCat("Geography & Locations")
            addCat("People & Groups")
            addCat("Culture & Society")
            addCat("History & Structure")
            addCat("Politics & Power")
            addCat("Magic & Technology")
        }
    }
    fun getCatCount() : Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $CAT_TABLE_NAME", null).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }
    fun getCategoryIcon(categoryName: String): Int {
        return when (categoryName) {
            "Geography & Locations" -> R.drawable.ic_world
            "People & Groups" -> R.drawable.ic_people
            "Culture & Society" -> R.drawable.ic_temple
            "History & Structure" -> R.drawable.ic_scroll
            "Politics & Power" -> R.drawable.ic_sword
            "Magic & Technology" -> R.drawable.ic_magic
            else -> R.drawable.ic_sword
        }
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
    fun getAllTags(): List<Tags> {
        val result = mutableListOf<Tags>()

        readableDatabase.query(
            TAG_TABLE_NAME,
            arrayOf(TAG_COLUMN_ID, TAG_COLUMN_TITLE, TAG_COLUMN_CATFK),
            null, null, null, null,
            "$TAG_COLUMN_TITLE ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(TAG_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(TAG_COLUMN_TITLE)
            val catFKIx = c.getColumnIndexOrThrow(TAG_COLUMN_CATFK)

            while (c.moveToNext()) {
                val tags = Tags().apply {
                    tagIDPK = c.getInt(idIx)
                    tagName = c.getString(titleIx)
                    tagCatFK = c.getInt(catFKIx)
                }
                result.add(tags)
            }
        }
        return result
    }
    fun getTagsByCategory(catId: Int): List<Tags> {
        val result = mutableListOf<Tags>()

        readableDatabase.query(
            TAG_TABLE_NAME,
            arrayOf(TAG_COLUMN_ID, TAG_COLUMN_TITLE, TAG_COLUMN_CATFK),
            "$TAG_COLUMN_CATFK = ?",
            arrayOf(catId.toString()),
            null, null,
            "$TAG_COLUMN_TITLE ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(TAG_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(TAG_COLUMN_TITLE)
            val catFKIx = c.getColumnIndexOrThrow(TAG_COLUMN_CATFK)

            while (c.moveToNext()) {
                val tag = Tags().apply {
                    tagIDPK = c.getInt(idIx)
                    tagName = c.getString(titleIx)
                    tagCatFK = c.getInt(catFKIx)
                }
                result.add(tag)
            }
        }

        return result
    }
    fun deleteTag(catId: Int): Int {
        return writableDatabase.delete(
            TAG_TABLE_NAME,
            "$TAG_COLUMN_ID = ?",
            arrayOf(catId.toString())
        )
    }
    fun getTagCount() : Int {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $TAG_TABLE_NAME", null).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }
    // INPUT DEFAULT TAGS
    fun addDefaultTags() {
        if (getTagCount() == 0) {
            // Look up category IDs
            val cats = getAllCats()
            val geo     = cats.find { it.catName == "Geography & Locations" }?.catIDPK ?: return
            val people  = cats.find { it.catName == "People & Groups" }?.catIDPK ?: return
            val culture = cats.find { it.catName == "Culture & Society" }?.catIDPK ?: return
            val history = cats.find { it.catName == "History & Structure" }?.catIDPK ?: return
            val politics= cats.find { it.catName == "Politics & Power" }?.catIDPK ?: return
            val magic   = cats.find { it.catName == "Magic & Technology" }?.catIDPK ?: return

            addTag("Continents", geo)
            addTag("Countries / Realms", geo)
            addTag("Regions", geo)
            addTag("Cities", geo)
            addTag("Towns", geo)
            addTag("Villages", geo)
            addTag("Landmarks", geo)
            addTag("Rivers", geo)
            addTag("Lakes", geo)
            addTag("Seas / Oceans", geo)
            addTag("Mountain Ranges", geo)
            addTag("Forests", geo)
            addTag("Deserts", geo)
            addTag("Islands", geo)
            addTag("Biomes", geo)

            addTag("Characters", people)
            addTag("Historical Figures", people)
            addTag("Rulers", people)
            addTag("Heroes / Legends", people)
            addTag("Prominent Families", people)
            addTag("Dynasties", people)
            addTag("Noble Houses", people)
            addTag("Organizations", people)
            addTag("Factions", people)
            addTag("Guilds", people)
            addTag("Orders", people)

            addTag("Cultures", culture)
            addTag("Ethnicities", culture)
            addTag("Species / Races", culture)
            addTag("Religions", culture)
            addTag("Deities", culture)
            addTag("Languages", culture)
            addTag("Traditions", culture)
            addTag("Customs", culture)
            addTag("Legal Systems", culture)

            addTag("Timeline", history)
            addTag("Historical Events", history)
            addTag("Wars", history)
            addTag("Treaties", history)
            addTag("Revolutions", history)
            addTag("Eras", history)

            addTag("Kingdoms", politics)
            addTag("Empires", politics)
            addTag("Republics", politics)
            addTag("Political Systems", politics)
            addTag("Alliances", politics)
            addTag("Conflicts", politics)

            addTag("Magic Systems", magic)
            addTag("Creatures", magic)
            addTag("Artifacts", magic)
            addTag("Technologies", magic)
            addTag("Prophecies", magic)
            addTag("Myths / Legends", magic)
        }
    }
    // NOTE FUNCTIONS
    fun addNote(title: String, content: String, brfdescr: String, tagId: Int?, worldId: Int?): Long {
        val values = ContentValues().apply {
            put(NOTE_COLUMN_TITLE, title)
            put(NOTE_COLUMN_CONTENT, content)
            put(NOTE_COLUMN_BRFDESCR, brfdescr)
            if (tagId != null) put(NOTE_COLUMN_TAGFK, tagId)
            if (worldId != null) put(NOTE_COLUMN_WORLDFK, worldId)
        }
        return writableDatabase.insert(NOTE_TABLE_NAME, null, values)
    }
    fun updateNote(noteId: Int, title: String, content: String, brfdescr: String = "", tagId: Int?, worldId: Int?): Int {
        val values = ContentValues().apply {
            put(NOTE_COLUMN_TITLE, title)
            put(NOTE_COLUMN_CONTENT, content)
            if (brfdescr.isNotEmpty()) put(NOTE_COLUMN_BRFDESCR, brfdescr)
            if (tagId != null) put(NOTE_COLUMN_TAGFK, tagId) else putNull(NOTE_COLUMN_TAGFK)
            if (worldId != null) put(NOTE_COLUMN_WORLDFK, worldId) else putNull(NOTE_COLUMN_WORLDFK)
        }
        return writableDatabase.update(
            NOTE_TABLE_NAME,
            values,
            "$NOTE_COLUMN_ID = ?",
            arrayOf(noteId.toString())
        )
    }
    fun getAllNotes(): List<Notes> {
        val result = mutableListOf<Notes>()

        readableDatabase.query(
            NOTE_TABLE_NAME,
            arrayOf(NOTE_COLUMN_ID, NOTE_COLUMN_TITLE, NOTE_COLUMN_CONTENT,NOTE_COLUMN_BRFDESCR, NOTE_COLUMN_TAGFK),
            null, null, null, null,
            "$NOTE_COLUMN_ID ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(NOTE_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TITLE)
            val contentIx = c.getColumnIndexOrThrow(NOTE_COLUMN_CONTENT)
            val brfdescrIx = c.getColumnIndexOrThrow(NOTE_COLUMN_BRFDESCR)
            val tagFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TAGFK)

            while (c.moveToNext()) {
                val notes = Notes().apply{
                    noteIDPK = c.getInt(idIx)
                    noteName = c.getString(titleIx)
                    noteDescr = c.getString(contentIx)
                    noteBrfDescr = c.getString(brfdescrIx)
                    noteTagFK = if (c.isNull(tagFKIx)) null else c.getInt(tagFKIx)

                }
                result.add(notes)
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
    fun getNotesByTag(tagId: Int): List<Notes> {
        val result = mutableListOf<Notes>()

        readableDatabase.query(
            NOTE_TABLE_NAME,
            arrayOf(
                NOTE_COLUMN_ID,
                NOTE_COLUMN_TITLE,
                NOTE_COLUMN_CONTENT,
                NOTE_COLUMN_BRFDESCR,
                NOTE_COLUMN_TAGFK,
                NOTE_COLUMN_WORLDFK
            ),
            "$NOTE_COLUMN_TAGFK = ?",
            arrayOf(tagId.toString()),
            null,
            null,
            "$NOTE_COLUMN_ID ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(NOTE_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TITLE)
            val contentIx = c.getColumnIndexOrThrow(NOTE_COLUMN_CONTENT)
            val brfdescrIx = c.getColumnIndexOrThrow(NOTE_COLUMN_BRFDESCR)
            val tagFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TAGFK)
            val worldFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_WORLDFK)

            while (c.moveToNext()) {
                val note = Notes().apply {
                    noteIDPK = c.getInt(idIx)
                    noteName = c.getString(titleIx)
                    noteDescr = c.getString(contentIx)
                    noteBrfDescr = c.getString(brfdescrIx)
                    noteTagFK = if (c.isNull(tagFKIx)) null else c.getInt(tagFKIx)
                    noteWorldFK = if (c.isNull(worldFKIx)) null else c.getInt(worldFKIx)
                }
                result.add(note)
            }
        }

        return result
    }
    fun getNotesByWorld(worldId: Int): List<Notes> {
        val result = mutableListOf<Notes>()

        readableDatabase.query(
            NOTE_TABLE_NAME,
            arrayOf(
                NOTE_COLUMN_ID,
                NOTE_COLUMN_TITLE,
                NOTE_COLUMN_CONTENT,
                NOTE_COLUMN_BRFDESCR,
                NOTE_COLUMN_TAGFK,
                NOTE_COLUMN_WORLDFK
            ),
            "$NOTE_COLUMN_WORLDFK = ?",
            arrayOf(worldId.toString()),
            null,
            null,
            "$NOTE_COLUMN_ID ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(NOTE_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TITLE)
            val contentIx = c.getColumnIndexOrThrow(NOTE_COLUMN_CONTENT)
            val brfdescrIx = c.getColumnIndexOrThrow(NOTE_COLUMN_BRFDESCR)
            val tagFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TAGFK)
            val worldFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_WORLDFK)

            while (c.moveToNext()) {
                val note = Notes().apply {
                    noteIDPK = c.getInt(idIx)
                    noteName = c.getString(titleIx)
                    noteDescr = c.getString(contentIx)
                    noteBrfDescr = c.getString(brfdescrIx)
                    noteTagFK = if (c.isNull(tagFKIx)) null else c.getInt(tagFKIx)
                    noteWorldFK = if (c.isNull(worldFKIx)) null else c.getInt(worldFKIx)
                }
                result.add(note)
            }
        }

        return result
    }
    fun getNotesByTag(tagId: Int, worldId: Int): List<Notes> {
        val result = mutableListOf<Notes>()

        readableDatabase.query(
            NOTE_TABLE_NAME,
            arrayOf(
                NOTE_COLUMN_ID,
                NOTE_COLUMN_TITLE,
                NOTE_COLUMN_CONTENT,
                NOTE_COLUMN_BRFDESCR,
                NOTE_COLUMN_TAGFK,
                NOTE_COLUMN_WORLDFK
            ),
            "$NOTE_COLUMN_TAGFK = ? AND $NOTE_COLUMN_WORLDFK = ?",
            arrayOf(tagId.toString(), worldId.toString()),
            null,
            null,
            "$NOTE_COLUMN_ID ASC"
        ).use { c ->

            val idIx = c.getColumnIndexOrThrow(NOTE_COLUMN_ID)
            val titleIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TITLE)
            val contentIx = c.getColumnIndexOrThrow(NOTE_COLUMN_CONTENT)
            val brfdescrIx = c.getColumnIndexOrThrow(NOTE_COLUMN_BRFDESCR)
            val tagFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TAGFK)
            val worldFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_WORLDFK)

            while (c.moveToNext()) {
                val note = Notes().apply {
                    noteIDPK = c.getInt(idIx)
                    noteName = c.getString(titleIx)
                    noteDescr = c.getString(contentIx)
                    noteBrfDescr = c.getString(brfdescrIx)
                    noteTagFK = if (c.isNull(tagFKIx)) null else c.getInt(tagFKIx)
                    noteWorldFK = if (c.isNull(worldFKIx)) null else c.getInt(worldFKIx)
                }
                result.add(note)
            }
        }

        return result
    }
    fun getNoteById(noteId: Int): Notes? {
        var note: Notes? = null

        readableDatabase.query(
            NOTE_TABLE_NAME,
            arrayOf(
                NOTE_COLUMN_ID,
                NOTE_COLUMN_TITLE,
                NOTE_COLUMN_CONTENT,
                NOTE_COLUMN_BRFDESCR,
                NOTE_COLUMN_TAGFK,
                NOTE_COLUMN_WORLDFK
            ),
            "$NOTE_COLUMN_ID = ?",
            arrayOf(noteId.toString()),
            null,
            null,
            null
        ).use { c ->

            if (c.moveToFirst()) {

                val idIx = c.getColumnIndexOrThrow(NOTE_COLUMN_ID)
                val titleIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TITLE)
                val contentIx = c.getColumnIndexOrThrow(NOTE_COLUMN_CONTENT)
                val brfdescrIx = c.getColumnIndexOrThrow(NOTE_COLUMN_BRFDESCR)
                val tagFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_TAGFK)
                val worldFKIx = c.getColumnIndexOrThrow(NOTE_COLUMN_WORLDFK)

                note = Notes().apply {
                    noteIDPK = c.getInt(idIx)
                    noteName = c.getString(titleIx)
                    noteDescr = c.getString(contentIx)
                    noteBrfDescr = c.getString(brfdescrIx)
                    noteTagFK = if (c.isNull(tagFKIx)) null else c.getInt(tagFKIx)
                    noteWorldFK = if (c.isNull(worldFKIx)) null else c.getInt(worldFKIx)
                }
            }
        }

        return note
    }
    fun getRecentNotes(limit: Int = 10, worldId: Int? = null): List<RecentNote> {
        val result = mutableListOf<RecentNote>()

        // Filter to only notes belonging to that worldID
        val worldFilter = if (worldId != null) "WHERE n.$NOTE_COLUMN_WORLDFK = ?" else ""
        val args = if (worldId != null) arrayOf(worldId.toString(), limit.toString())
        else arrayOf(limit.toString())

        val query = """
        SELECT n.$NOTE_COLUMN_ID, n.$NOTE_COLUMN_TITLE, n.$NOTE_COLUMN_BRFDESCR, t.$TAG_COLUMN_TITLE AS tagName
        FROM $NOTE_TABLE_NAME n
        LEFT JOIN $TAG_TABLE_NAME t ON n.$NOTE_COLUMN_TAGFK = t.$TAG_COLUMN_ID
        $worldFilter
        ORDER BY n.$NOTE_COLUMN_ID DESC
        LIMIT ?
    """.trimIndent()

        readableDatabase.rawQuery(query, args).use { c ->
            val idIx       = c.getColumnIndexOrThrow(NOTE_COLUMN_ID)
            val titleIx    = c.getColumnIndexOrThrow(NOTE_COLUMN_TITLE)
            val brfDescrIx = c.getColumnIndexOrThrow(NOTE_COLUMN_BRFDESCR)
            val tagNameIx  = c.getColumnIndexOrThrow("tagName")

            while (c.moveToNext()) {
                result.add(
                    RecentNote(
                        noteIDPK     = c.getInt(idIx),
                        noteName     = c.getString(titleIx),
                        noteBrfDescr = c.getString(brfDescrIx),
                        tagName      = if (c.isNull(tagNameIx)) null else c.getString(tagNameIx)
                    )
                )
            }
        }

        return result
    }
}
