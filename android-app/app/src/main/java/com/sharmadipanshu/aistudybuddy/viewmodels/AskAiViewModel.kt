package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.AIChatRequest
import com.sharmadipanshu.aistudybuddy.models.AIChatResponse
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.NoteInteractionRequest
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AskAiViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _responseState = MutableLiveData<UiState<AIChatResponse>>(UiState.Idle)
    val responseState: LiveData<UiState<AIChatResponse>> = _responseState

    fun askQuestion(question: String, noteId: String? = null) {
        if (question.isBlank()) {
            _responseState.value = UiState.Error("Please enter a question.")
            return
        }

            _responseState.value = UiState.Loading
        viewModelScope.launch {
            if (noteId.isNullOrBlank()) {
                when (val result = studyRepository.askGeneralAi(AIChatRequest(message = question.trim()))) {
                    is ApiResult.Success -> _responseState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _responseState.value = UiState.Error(result.message)
                }
            } else {
                when (
                    val result = studyRepository.interactWithNote(
                        NoteInteractionRequest(
                            noteId = noteId,
                            pageNumber = 1,
                            question = question.trim(),
                            selectedText = "",
                            xCoordinate = 0f,
                            yCoordinate = 0f
                        )
                    )
                ) {
                    is ApiResult.Success -> {
                        _responseState.value = UiState.Success(
                            AIChatResponse(
                                reply = result.data.answer,
                                retrievedChunks = result.data.retrievedChunks
                            )
                        )
                    }
                    is ApiResult.Error -> _responseState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun clearState() {
        _responseState.value = UiState.Idle
    }
}
