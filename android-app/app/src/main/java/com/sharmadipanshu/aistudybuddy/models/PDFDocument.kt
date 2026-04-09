package com.sharmadipanshu.aistudybuddy.models

data class PDFDocument(
    val noteId: String = "",
    val title: String,
    val localPath: String? = null,
    val remoteUrl: String? = null
)
