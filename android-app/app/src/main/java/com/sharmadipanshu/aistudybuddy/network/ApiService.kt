package com.sharmadipanshu.aistudybuddy.network

import com.sharmadipanshu.aistudybuddy.models.AiQueryRequest
import com.sharmadipanshu.aistudybuddy.models.AiQueryResponse
import com.sharmadipanshu.aistudybuddy.models.NotesResponse
import com.sharmadipanshu.aistudybuddy.models.StudyRoomsResponse
import com.sharmadipanshu.aistudybuddy.models.UploadNotesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
    suspend fun askAi(@Body request: AiQueryRequest): Response<AiQueryResponse>

    @GET("study-rooms")
    suspend fun fetchStudyRooms(): Response<StudyRoomsResponse>
}
