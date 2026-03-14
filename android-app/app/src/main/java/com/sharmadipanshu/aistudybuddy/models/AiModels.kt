package com.sharmadipanshu.aistudybuddy.models

data class AiQueryRequest(
    val prompt: String
)

data class AiQueryResponse(
    val answer: String,
    val confidence: String? = null
)
