package com.example.playgod

class Worlds {
    var worldIDPK: Int = 0
        set(value) {
            if (value >= 0) field = value
        }

    var worldName: String = ""
        set(value) {
            if (value.isNotEmpty()) field = value
        }
    constructor() {}
    constructor(ID: Int, Name: String) {
        this.worldIDPK = ID;
        this.worldName = Name;
    }
}