package com.sharmadipanshu.aistudybuddy.models

data class StudyRoomsResponse(
    val rooms: List<StudyRoom> = emptyList()
)

data class StudyRoom(
    val id: String,
    val title: String,
    val topic: String,
    val memberCount: Int,
    val nextSession: String
)
