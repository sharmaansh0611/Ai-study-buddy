package com.sharmadipanshu.aistudybuddy.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sharmadipanshu.aistudybuddy.models.GoogleSignInOutcome
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) {

    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser?.isEmailVerified == true

    suspend fun login(email: String, password: String): Boolean {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: firebaseAuth.currentUser

        return if (user?.isEmailVerified == true) {
            FirebaseAuth.getInstance().currentUser
                ?.getIdToken(true)
                ?.addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val token = task.result.token

                        Log.d("FIREBASE_TOKEN", token!!)
                    }
                }

            userRepository.ensureUserProfile()
            true
        } else {
            user?.sendEmailVerification()?.await()
            firebaseAuth.signOut()
            false
        }
    }

    suspend fun signup(email: String, password: String) {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        userRepository.createUserProfile()
        authResult.user?.sendEmailVerification()?.await()
        firebaseAuth.signOut()
    }

    suspend fun signup(email: String, password: String, name: String, phoneNumber: String) {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        userRepository.createUserProfile(nameOverride = name, phoneNumber = phoneNumber)
        authResult.user?.sendEmailVerification()?.await()
        firebaseAuth.signOut()
    }

    suspend fun signInWithGoogle(idToken: String): GoogleSignInOutcome {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        return if (authResult.user?.isEmailVerified != false) {
            val profile = userRepository.ensureUserProfile(nameOverride = authResult.user?.displayName)
            GoogleSignInOutcome(
                isAuthenticated = true,
                requiresPhoneNumber = profile.phone.isBlank()
            )
        } else {
            GoogleSignInOutcome(isAuthenticated = false, requiresPhoneNumber = false)
        }
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserEmail(): String = firebaseAuth.currentUser?.email.orEmpty()
}
