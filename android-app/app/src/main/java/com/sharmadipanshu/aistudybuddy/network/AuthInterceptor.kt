package com.sharmadipanshu.aistudybuddy.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        val idToken = runBlocking {
            runCatching {
                // Firebase ID token is attached to every authenticated backend request.
                currentUser.getIdToken(true).await().token
            }.getOrNull()
        }

        if (idToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $idToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
