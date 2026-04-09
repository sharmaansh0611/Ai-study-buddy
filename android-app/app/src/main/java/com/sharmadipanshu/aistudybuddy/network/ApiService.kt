package com.sharmadipanshu.aistudybuddy.network

import com.sharmadipanshu.aistudybuddy.models.AIChatRequest
import com.sharmadipanshu.aistudybuddy.models.AIChatResponse
import com.sharmadipanshu.aistudybuddy.models.DeleteNoteResponse
import com.sharmadipanshu.aistudybuddy.models.EventsResponse
import com.sharmadipanshu.aistudybuddy.models.FlashcardsResponse
import com.sharmadipanshu.aistudybuddy.models.NoteInteractionRequest
import com.sharmadipanshu.aistudybuddy.models.NoteInteractionResponse
import com.sharmadipanshu.aistudybuddy.models.NotesResponse
import com.sharmadipanshu.aistudybuddy.models.PageNotes
import com.sharmadipanshu.aistudybuddy.models.PageSummaryRequest
import com.sharmadipanshu.aistudybuddy.models.QuizResponse
import com.sharmadipanshu.aistudybuddy.models.StudyEventRequest
import com.sharmadipanshu.aistudybuddy.models.StudyEventResponse
import com.sharmadipanshu.aistudybuddy.models.StudyRoomsResponse
import com.sharmadipanshu.aistudybuddy.models.TextToolRequest
import com.sharmadipanshu.aistudybuddy.models.UploadNotesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET(".")
    suspend fun getApiStatus(): Response<Map<String, String>>

    @GET("my-notes")
    suspend fun fetchNotes(): Response<NotesResponse>

    @Multipart
    @POST("upload-note")
    suspend fun uploadNotes(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody
    ): Response<UploadNotesResponse>

    @POST("ai/ask")
    suspend fun askAi(@Body request: AIChatRequest): Response<AIChatResponse>

    @POST("ai/chat")
    suspend fun askGeneralAi(@Body request: AIChatRequest): Response<AIChatResponse>

    @POST("ai/chat-notes")
    suspend fun chatWithNotes(@Body request: AIChatRequest): Response<AIChatResponse>

    @POST("ai/summarize-note")
    suspend fun summarizeNote(@Body request: TextToolRequest): Response<AIChatResponse>

    @POST("ai/generate-quiz")
    suspend fun generateQuiz(@Body request: TextToolRequest): Response<QuizResponse>

    @POST("ai/generate-flashcards")
    suspend fun generateFlashcards(@Body request: TextToolRequest): Response<FlashcardsResponse>

    @POST("ai/summarize-page")
    suspend fun summarizePage(@Body request: PageSummaryRequest): Response<PageNotes>

    @POST("note-interaction")
    suspend fun interactWithNote(@Body request: NoteInteractionRequest): Response<NoteInteractionResponse>

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") noteId: String): Response<DeleteNoteResponse>

    @GET("study-rooms")
    suspend fun fetchStudyRooms(): Response<StudyRoomsResponse>

    @POST("create-event")
    suspend fun createEvent(@Body request: StudyEventRequest): Response<StudyEventResponse>

    @GET("events")
    suspend fun fetchEvents(): Response<EventsResponse>
}
