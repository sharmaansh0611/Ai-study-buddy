package com.sharmadipanshu.aistudybuddy.repository

import com.sharmadipanshu.aistudybuddy.models.AiQueryRequest
import com.sharmadipanshu.aistudybuddy.models.AiQueryResponse
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.Note
import com.sharmadipanshu.aistudybuddy.models.StudyRoom
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

    suspend fun askAi(request: AiQueryRequest): ApiResult<AiQueryResponse> =
        apiCall(
            request = { apiService.askAi(request) },
            mapper = { response -> response }
        )

    suspend fun fetchStudyRooms(): ApiResult<List<StudyRoom>> =
        apiCall(
            request = { apiService.fetchStudyRooms() },
            mapper = { response -> response.rooms }
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
