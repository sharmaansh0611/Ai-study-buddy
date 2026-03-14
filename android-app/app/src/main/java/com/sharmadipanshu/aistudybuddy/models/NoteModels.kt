package com.sharmadipanshu.aistudybuddy.models

data class NotesResponse(
    val notes: List<Note> = emptyList()
)

data class Note(
    val id: String,
    val title: String,
    val subject: String,
    val updatedAt: String,
    val fileUrl: String? = null
)

data class UploadNotesRequest(
    val title: String,
    val subject: String,
    val fileUrl: String
)

data class UploadNotesResponse(
    val success: Boolean,
    val message: String
)
