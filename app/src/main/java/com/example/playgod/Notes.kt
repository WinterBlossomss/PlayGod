package com.example.playgod

class Notes {
    var noteIDPK : Int = 0
        get () = field
        set(value){if (value>=0) field = value;}

    var noteName : String = ""
        get () = field
        set (value){ if (value.isNotEmpty()) field = value; }
    var noteDescr : String = ""
        get () = field
        set (value){ if (value.isNotEmpty()) field = value; }
    var noteBrfDescr : String = ""
        get () = field
        set (value){ if (value.isNotEmpty()) field = value; }
    var noteTagFK: Int? = null
        set(value) {
            if (value == null || value >= 0) field = value
        }
    var noteWorldFK: Int? = null
        set(value) {
            if (value == null || value >= 0) field = value
        }

    constructor() {}
    constructor(ID: Int, Name: String, Description: String, BriefDescription: String, TagFK : Int, WorldFK : Int)
    {
        this.noteIDPK = ID;
        this.noteName = Name;
        this.noteDescr = Description;
        this.noteBrfDescr = BriefDescription;
        this.noteTagFK = TagFK;
        this.noteWorldFK = WorldFK;
    }
}