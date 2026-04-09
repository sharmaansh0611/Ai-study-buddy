package com.sharmadipanshu.aistudybuddy.repository

import com.sharmadipanshu.aistudybuddy.models.AIChatRequest
import com.sharmadipanshu.aistudybuddy.models.AIChatResponse
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.DeleteNoteResponse
import com.sharmadipanshu.aistudybuddy.models.Flashcard
import com.sharmadipanshu.aistudybuddy.models.Note
import com.sharmadipanshu.aistudybuddy.models.NoteInteractionRequest
import com.sharmadipanshu.aistudybuddy.models.NoteInteractionResponse
import com.sharmadipanshu.aistudybuddy.models.PageNotes
import com.sharmadipanshu.aistudybuddy.models.PageSummaryRequest
import com.sharmadipanshu.aistudybuddy.models.QuizQuestion
import com.sharmadipanshu.aistudybuddy.models.StudyEvent
import com.sharmadipanshu.aistudybuddy.models.StudyEventRequest
import com.sharmadipanshu.aistudybuddy.models.StudyRoom
import com.sharmadipanshu.aistudybuddy.models.TextToolRequest
import com.sharmadipanshu.aistudybuddy.models.UploadNotesResponse
import com.sharmadipanshu.aistudybuddy.network.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun fetchNotes(): ApiResult<List<Note>> =
        apiCall(
            request = { apiService.fetchNotes() },
            mapper = { response -> response.notes }
        )

    suspend fun uploadNotes(title: String, pdfFile: File): ApiResult<UploadNotesResponse> {
        val titleBody = title.toRequestBody("text/plain".toMediaType())
        val fileBody = pdfFile.asRequestBody("application/pdf".toMediaType())
        val multipartFile = MultipartBody.Part.createFormData("file", pdfFile.name, fileBody)

        return apiCall(
            request = { apiService.uploadNotes(multipartFile, titleBody) },
            mapper = { response -> response }
        )
    }

    suspend fun askAi(request: AIChatRequest): ApiResult<AIChatResponse> =
        apiCall(
            request = { apiService.askAi(request) },
            mapper = { response -> response }
        )

    suspend fun askGeneralAi(request: AIChatRequest): ApiResult<AIChatResponse> =
        apiCall(
            request = { apiService.askGeneralAi(request) },
            mapper = { response -> response }
        )

    suspend fun chatWithNotes(question: String): ApiResult<AIChatResponse> =
        apiCall(
            request = { apiService.chatWithNotes(AIChatRequest(message = question)) },
            mapper = { response -> response }
        )

    suspend fun summarizeNote(text: String): ApiResult<AIChatResponse> =
        apiCall(
            request = { apiService.summarizeNote(TextToolRequest(text = text)) },
            mapper = { response -> response }
        )

    suspend fun generateQuiz(text: String): ApiResult<List<QuizQuestion>> =
        apiCall(
            request = { apiService.generateQuiz(TextToolRequest(text = text)) },
            mapper = { response -> response.quiz }
        )

    suspend fun generateFlashcards(text: String): ApiResult<List<Flashcard>> =
        apiCall(
            request = { apiService.generateFlashcards(TextToolRequest(text = text)) },
            mapper = { response -> response.flashcards }
        )

    suspend fun deleteNote(noteId: String): ApiResult<DeleteNoteResponse> =
        apiCall(
            request = { apiService.deleteNote(noteId) },
            mapper = { response -> response }
        )

    suspend fun summarizePage(pageText: String, pageNumber: Int): ApiResult<PageNotes> =
        apiCall(
            request = {
                apiService.summarizePage(
                    PageSummaryRequest(
                        pageText = pageText,
                        pageNumber = pageNumber
                    )
                )
            },
            mapper = { response -> response }
        )

    suspend fun interactWithNote(request: NoteInteractionRequest): ApiResult<NoteInteractionResponse> =
        apiCall(
            request = { apiService.interactWithNote(request) },
            mapper = { response -> response }
        )

    suspend fun fetchStudyRooms(): ApiResult<List<StudyRoom>> =
        apiCall(
            request = { apiService.fetchStudyRooms() },
            mapper = { response -> response.rooms }
        )

    suspend fun createEvent(request: StudyEventRequest): ApiResult<StudyEvent> =
        apiCall(
            request = { apiService.createEvent(request) },
            mapper = { response -> response.event }
        )

    suspend fun fetchEvents(): ApiResult<List<StudyEvent>> =
        apiCall(
            request = { apiService.fetchEvents() },
            mapper = { response -> response.events }
        )

    suspend fun fetchApiStatus(): ApiResult<String> =
        apiCall(
            request = { apiService.getApiStatus() },
            mapper = { response ->
                response["message"] ?: response["status"] ?: "API connected"
            }
        )

    private suspend fun <T, R> apiCall(
        request: suspend () -> Response<T>,
        mapper: (T) -> R
    ): ApiResult<R> = try {
        val response = request()
        if (response.isSuccessful && response.body() != null) {
            ApiResult.Success(mapper(response.body()!!))
        } else {
            ApiResult.Error(response.message().ifBlank { "Something went wrong. Please try again." })
        }
    } catch (exception: Exception) {
        ApiResult.Error(exception.localizedMessage ?: "Unable to reach the server.")
    }
}
