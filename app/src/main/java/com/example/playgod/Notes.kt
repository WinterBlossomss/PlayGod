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
    var noteTagFK: Int? = null
        set(value) {
            if (value == null || value >= 0) field = value
        }
}