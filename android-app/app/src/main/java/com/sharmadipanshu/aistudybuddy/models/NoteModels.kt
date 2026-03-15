package com.sharmadipanshu.aistudybuddy.models

import com.google.gson.annotations.SerializedName

data class NotesResponse(
    val notes: List<Note> = emptyList()
)

data class Note(
    @SerializedName("note_id")
    val noteId: String,
    val title: String,
    @SerializedName("file_url")
    val fileUrl: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class UploadNotesResponse(
    val message: String,
    @SerializedName("note_id")
    val noteId: String,
    val title: String,
    @SerializedName("file_url")
    val fileUrl: String
)
