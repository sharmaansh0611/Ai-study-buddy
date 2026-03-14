package com.sharmadipanshu.aistudybuddy.network

import com.sharmadipanshu.aistudybuddy.models.AiQueryRequest
import com.sharmadipanshu.aistudybuddy.models.AiQueryResponse
import com.sharmadipanshu.aistudybuddy.models.NotesResponse
import com.sharmadipanshu.aistudybuddy.models.StudyRoomsResponse
import com.sharmadipanshu.aistudybuddy.models.UploadNotesRequest
import com.sharmadipanshu.aistudybuddy.models.UploadNotesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET(".")
    suspend fun getApiStatus(): Response<Map<String, String>>

    @GET("notes")
    suspend fun fetchNotes(): Response<NotesResponse>

    @POST("notes/upload")
    suspend fun uploadNotes(@Body request: UploadNotesRequest): Response<UploadNotesResponse>

    @POST("ai/ask")
    suspend fun askAi(@Body request: AiQueryRequest): Response<AiQueryResponse>

    @GET("study-rooms")
    suspend fun fetchStudyRooms(): Response<StudyRoomsResponse>
}
