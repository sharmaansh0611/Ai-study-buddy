package com.sharmadipanshu.aistudybuddy.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp? = null
)
