package com.sharmadipanshu.aistudybuddy.models

import com.google.gson.annotations.SerializedName

data class TextToolRequest(
    val text: String,
    val prompt: String? = null
)

data class QuizQuestion(
    val question: String,
    val options: List<String> = emptyList(),
    val answer: String
)

data class QuizResponse(
    val quiz: List<QuizQuestion> = emptyList()
)

data class Flashcard(
    val question: String,
    val answer: String
)

data class FlashcardsResponse(
    val flashcards: List<Flashcard> = emptyList()
)

data class StudyEventRequest(
    val title: String,
    val description: String,
    val date: String,
    val time: String
)

data class StudyEventResponse(
    val message: String,
    val event: StudyEvent
)

data class EventsResponse(
    val events: List<StudyEvent> = emptyList()
)

data class StudyEvent(
    @SerializedName("event_id")
    val eventId: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    @SerializedName("created_at")
    val createdAt: String
)
