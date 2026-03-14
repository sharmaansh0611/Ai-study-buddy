package com.sharmadipanshu.aistudybuddy.network

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("health")
    fun getHealth(): Call<Map<String, String>>

}
