package com.example.playgod;

class Tags {
    var tagIDPK: Int = 0
        set(value) { if (value >= 0) field = value }

    var tagName: String = ""
        set(value) { if (value.isNotEmpty()) field = value }
}
