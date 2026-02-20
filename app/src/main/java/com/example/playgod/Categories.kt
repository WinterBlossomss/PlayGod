package com.example.playgod

class Categories {

    var catIDPK: Int = 0
        set(value) {
            if (value >= 0) field = value
        }

    var catName: String = ""
        set(value) {
            if (value.isNotEmpty()) field = value
        }
    constructor()
    constructor(ID: Int, Name: String) {
        this.catIDPK = ID
        this.catName = Name
    }
}