package com.example.playgod;

class Tags {
    var tagIDPK: Int = 0
        set(value) {
            if (value >= 0) field = value
        }

    var tagName: String = ""
        set(value) {
            if (value.isNotEmpty()) field = value
        }
    var tagCatFK: Int = 0
        set(value) {
            if (value >= 0) field = value
        }

    constructor() {}
    constructor(ID: Int, Name: String,CatFK : Int) {
        this.tagIDPK = ID;
        this.tagName = Name;
        this.tagCatFK = CatFK;
    }
}
