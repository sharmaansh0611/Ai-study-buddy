package com.sharmadipanshu.aistudybuddy.models

import com.google.gson.annotations.SerializedName

data class AIChatRequest(
    val message: String? = null,
    val text: String? = null,
    val prompt: String? = null
)

data class NoteInteractionRequest(
    @SerializedName("note_id")
    val noteId: String,
    @SerializedName("page_number")
    val pageNumber: Int,
    val question: String,
    @SerializedName("selected_text")
    val selectedText: String,
    @SerializedName("x_coordinate")
    val xCoordinate: Float,
    @SerializedName("y_coordinate")
    val yCoordinate: Float
)

data class RetrievedChunk(
    @SerializedName("note_id")
    val noteId: String? = null,
    @SerializedName("page_number")
    val pageNumber: Int? = null,
    @SerializedName("chunk_text")
    val chunkText: String? = null,
    val score: Double? = null
)

data class AIChatResponse(
    val reply: String,
    @SerializedName("retrieved_chunks")
    val retrievedChunks: List<RetrievedChunk> = emptyList()
)

data class NoteInteractionResponse(
    val answer: String,
    @SerializedName("retrieved_chunks")
    val retrievedChunks: List<RetrievedChunk> = emptyList()
)

data class PageSummaryRequest(
    @SerializedName("page_text")
    val pageText: String,
    @SerializedName("page_number")
    val pageNumber: Int
)

data class PageNotes(
    val summary: String,
    @SerializedName("key_points")
    val keyPoints: List<String> = emptyList()
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)
