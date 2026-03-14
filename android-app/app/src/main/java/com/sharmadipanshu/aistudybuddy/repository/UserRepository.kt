package com.sharmadipanshu.aistudybuddy.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sharmadipanshu.aistudybuddy.models.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val usersCollection = firestore.collection(USERS_COLLECTION)

    suspend fun createUserProfile(nameOverride: String? = null): User {
        val currentUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user available")

        val documentReference = usersCollection.document(currentUser.uid)
        val existingProfile = documentReference.get().await()
        if (!existingProfile.exists()) {
            // The profile document is keyed by Firebase UID so app data can be linked back to auth.
            documentReference.set(
                mapOf(
                    "userId" to currentUser.uid,
                    "name" to resolveDisplayName(currentUser.displayName, currentUser.email, nameOverride),
                    "email" to currentUser.email.orEmpty(),
                    "created_at" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        return getUserProfile()
            ?: User(
                userId = currentUser.uid,
                name = resolveDisplayName(currentUser.displayName, currentUser.email, nameOverride),
                email = currentUser.email.orEmpty(),
                createdAt = Timestamp.now()
            )
    }

    suspend fun getUserProfile(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null
        val snapshot = usersCollection.document(currentUser.uid).get().await()
        if (!snapshot.exists()) return null

        return snapshot.toObject(User::class.java)?.copy(userId = currentUser.uid)
    }

    suspend fun checkUserExists(): Boolean {
        val currentUser = firebaseAuth.currentUser ?: return false
        return usersCollection.document(currentUser.uid).get().await().exists()
    }

    suspend fun ensureUserProfile(nameOverride: String? = null): User {
        return if (checkUserExists()) {
            getUserProfile()
                ?: createUserProfile(nameOverride)
        } else {
            createUserProfile(nameOverride)
        }
    }

    private fun resolveDisplayName(
        authDisplayName: String?,
        email: String?,
        nameOverride: String?
    ): String {
        if (!nameOverride.isNullOrBlank()) return nameOverride.trim()
        if (!authDisplayName.isNullOrBlank()) return authDisplayName.trim()

        val emailPrefix = email.orEmpty().substringBefore("@").replace('.', ' ').replace('_', ' ')
        return emailPrefix
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
            .ifBlank { "AI Study Buddy User" }
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
