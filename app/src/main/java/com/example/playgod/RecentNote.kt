package com.example.playgod

data class RecentNote(
    val noteIDPK: Int,
    val noteName: String,
    val noteBrfDescr: String,
    val tagName: String?  // null if the note has no tag assigned
)