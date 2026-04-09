package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sharmadipanshu.aistudybuddy.models.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AIInteractionViewModel @Inject constructor() : ViewModel() {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _contextText = MutableLiveData("")
    val contextText: LiveData<String> = _contextText

    fun updateContextText(text: String) {
        _contextText.value = text
    }

    fun addUserQuestion(question: String) {
        _chatMessages.value = _chatMessages.value.orEmpty() + ChatMessage(question, isUser = true)
    }

    fun addAiAnswer(answer: String) {
        _chatMessages.value = _chatMessages.value.orEmpty() + ChatMessage(answer, isUser = false)
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }
}
