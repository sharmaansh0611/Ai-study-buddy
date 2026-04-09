package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.AIChatResponse
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.Flashcard
import com.sharmadipanshu.aistudybuddy.models.QuizQuestion
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyToolsViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _notesChatState = MutableLiveData<UiState<AIChatResponse>>(UiState.Idle)
    val notesChatState: LiveData<UiState<AIChatResponse>> = _notesChatState

    private val _summaryState = MutableLiveData<UiState<AIChatResponse>>(UiState.Idle)
    val summaryState: LiveData<UiState<AIChatResponse>> = _summaryState

    private val _quizState = MutableLiveData<UiState<List<QuizQuestion>>>(UiState.Idle)
    val quizState: LiveData<UiState<List<QuizQuestion>>> = _quizState

    private val _flashcardsState = MutableLiveData<UiState<List<Flashcard>>>(UiState.Idle)
    val flashcardsState: LiveData<UiState<List<Flashcard>>> = _flashcardsState

    fun askNotes(question: String) {
        if (question.isBlank()) {
            _notesChatState.value = UiState.Error("Enter a question to ask your notes.")
            return
        }

        _notesChatState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.chatWithNotes(question.trim())) {
                is ApiResult.Success -> _notesChatState.value = UiState.Success(result.data)
                is ApiResult.Error -> _notesChatState.value = UiState.Error(result.message)
            }
        }
    }

    fun summarize(text: String) {
        if (text.isBlank()) {
            _summaryState.value = UiState.Error("Paste note content to summarize.")
            return
        }

        _summaryState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.summarizeNote(text.trim())) {
                is ApiResult.Success -> _summaryState.value = UiState.Success(result.data)
                is ApiResult.Error -> _summaryState.value = UiState.Error(result.message)
            }
        }
    }

    fun generateQuiz(text: String) {
        if (text.isBlank()) {
            _quizState.value = UiState.Error("Paste note content to generate a quiz.")
            return
        }

        _quizState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.generateQuiz(text.trim())) {
                is ApiResult.Success -> _quizState.value = UiState.Success(result.data)
                is ApiResult.Error -> _quizState.value = UiState.Error(result.message)
            }
        }
    }

    fun generateFlashcards(text: String) {
        if (text.isBlank()) {
            _flashcardsState.value = UiState.Error("Paste note content to generate flashcards.")
            return
        }

        _flashcardsState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.generateFlashcards(text.trim())) {
                is ApiResult.Success -> _flashcardsState.value = UiState.Success(result.data)
                is ApiResult.Error -> _flashcardsState.value = UiState.Error(result.message)
            }
        }
    }
}
