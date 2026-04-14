package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.AIChatRequest
import com.sharmadipanshu.aistudybuddy.models.AIChatResponse
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.Flashcard
import com.sharmadipanshu.aistudybuddy.models.PDFDocument
import com.sharmadipanshu.aistudybuddy.models.QuizQuestion
import com.sharmadipanshu.aistudybuddy.repository.PDFRepository
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PDFReaderViewModel @Inject constructor(
    private val pdfRepository: PDFRepository,
    private val studyRepository: StudyRepository
) : ViewModel() {

    // -- PDF file resolution state --
    private val _pdfFileState = MutableLiveData<UiState<File>>(UiState.Idle)
    val pdfFileState: LiveData<UiState<File>> = _pdfFileState

    private val _documentTitle = MutableLiveData<String>()
    val documentTitle: LiveData<String> = _documentTitle

    // -- AI response state --
    private val _aiResponseState = MutableLiveData<UiState<AIChatResponse>>(UiState.Idle)
    val aiResponseState: LiveData<UiState<AIChatResponse>> = _aiResponseState

    private val _quizState = MutableLiveData<UiState<List<QuizQuestion>>>(UiState.Idle)
    val quizState: LiveData<UiState<List<QuizQuestion>>> = _quizState

    private val _flashcardsState = MutableLiveData<UiState<List<Flashcard>>>(UiState.Idle)
    val flashcardsState: LiveData<UiState<List<Flashcard>>> = _flashcardsState

    // -- Current note ID, needed for the /ask API --
    private var currentNoteId: String = ""

    private var isLoaded = false

    fun loadDocument(document: PDFDocument) {
        if (isLoaded) return

        currentNoteId = document.noteId
        _documentTitle.value = document.title
        _pdfFileState.value = UiState.Loading

        viewModelScope.launch {
            runCatching {
                pdfRepository.resolvePdfFile(document)
            }.onSuccess { file ->
                isLoaded = true
                _pdfFileState.value = UiState.Success(file)
            }.onFailure { throwable ->
                _pdfFileState.value = UiState.Error(
                    throwable.localizedMessage ?: "Unable to open the PDF."
                )
            }
        }
    }

    /**
     * Sends the selected text with an action prefix (Explain / Summarize / Ask AI question)
     * to the backend using the existing /ai/ask endpoint.
     *
     * The request body mirrors: POST /ask { "query": "<action>: <text>", "docId": "<noteId>" }
     */
    fun runAiAction(selectedText: String, action: String) {
        val normalized = selectedText.trim()
        if (normalized.isBlank()) {
            _aiResponseState.value = UiState.Error("Select readable text first.")
            return
        }

        _aiResponseState.value = UiState.Loading
        viewModelScope.launch {
            when (
                val result = studyRepository.askAi(
                    AIChatRequest(
                        text = normalized,
                        prompt = action
                    )
                )
            ) {
                is ApiResult.Success -> _aiResponseState.value = UiState.Success(result.data)
                is ApiResult.Error -> _aiResponseState.value = UiState.Error(result.message)
            }
        }
    }

    fun askFollowUpQuestion(selectedText: String, question: String) {
        runAiAction(selectedText = selectedText, action = question)
    }

    fun clearAiResponseState() {
        _aiResponseState.value = UiState.Idle
        _quizState.value = UiState.Idle
        _flashcardsState.value = UiState.Idle
    }

    fun generateQuizFromText(text: String) {
        val normalized = text.trim()
        if (normalized.isBlank()) {
            _quizState.value = UiState.Error("Select text to generate a quiz.")
            return
        }

        _quizState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.generateQuiz(normalized)) {
                is ApiResult.Success -> _quizState.value = UiState.Success(result.data)
                is ApiResult.Error -> _quizState.value = UiState.Error(result.message)
            }
        }
    }

    fun generateFlashcardsFromText(text: String) {
        val normalized = text.trim()
        if (normalized.isBlank()) {
            _flashcardsState.value = UiState.Error("Select text to generate flashcards.")
            return
        }

        _flashcardsState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.generateFlashcards(normalized)) {
                is ApiResult.Success -> _flashcardsState.value = UiState.Success(result.data)
                is ApiResult.Error -> _flashcardsState.value = UiState.Error(result.message)
            }
        }
    }
}
