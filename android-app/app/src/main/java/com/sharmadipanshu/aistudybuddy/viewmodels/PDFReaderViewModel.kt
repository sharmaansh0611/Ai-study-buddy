package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.AIChatRequest
import com.sharmadipanshu.aistudybuddy.models.AIChatResponse
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.PDFDocument
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

    private val _pdfFileState = MutableLiveData<UiState<File>>(UiState.Idle)
    val pdfFileState: LiveData<UiState<File>> = _pdfFileState

    private val _documentTitle = MutableLiveData<String>()
    val documentTitle: LiveData<String> = _documentTitle

    private val _currentPageLabel = MutableLiveData("Page 1")
    val currentPageLabel: LiveData<String> = _currentPageLabel

    private val _currentPageText = MutableLiveData("")
    val currentPageText: LiveData<String> = _currentPageText

    private val _aiResponseState = MutableLiveData<UiState<AIChatResponse>>(UiState.Idle)
    val aiResponseState: LiveData<UiState<AIChatResponse>> = _aiResponseState

    private var isLoaded = false
    private var currentFile: File? = null
    private var currentDocument: PDFDocument? = null

    fun loadDocument(document: PDFDocument) {
        if (isLoaded) return

        currentDocument = document
        _documentTitle.value = document.title
        _pdfFileState.value = UiState.Loading

        viewModelScope.launch {
            runCatching {
                pdfRepository.resolvePdfFile(document)
            }.onSuccess { file ->
                isLoaded = true
                currentFile = file
                _pdfFileState.value = UiState.Success(file)
                extractPageText(0)
            }.onFailure { throwable ->
                _pdfFileState.value = UiState.Error(
                    throwable.localizedMessage ?: "Unable to open the PDF."
                )
            }
        }
    }

    fun onPageChanged(page: Int, pageCount: Int) {
        val currentPage = page + 1
        _currentPageLabel.value = "Page $currentPage of $pageCount"
        extractPageText(page)
    }

    fun extractPageText(page: Int) {
        val file = currentFile ?: return

        viewModelScope.launch {
            runCatching {
                pdfRepository.extractPageText(file, page)
            }.onSuccess { text ->
                _currentPageText.value = text
            }.onFailure {
                _currentPageText.value = ""
            }
        }
    }

    fun runAiAction(selectedText: String, prompt: String) {
        val normalizedText = selectedText.trim()
        if (normalizedText.isBlank()) {
            _aiResponseState.value = UiState.Error("Select readable text first.")
            return
        }

        _aiResponseState.value = UiState.Loading
        viewModelScope.launch {
            when (
                val result = studyRepository.askAi(
                    AIChatRequest(
                        text = normalizedText,
                        prompt = prompt
                    )
                )
            ) {
                is ApiResult.Success -> _aiResponseState.value = UiState.Success(result.data)
                is ApiResult.Error -> _aiResponseState.value = UiState.Error(result.message)
            }
        }
    }

    fun askFollowUpQuestion(selectedText: String, question: String) {
        runAiAction(
            selectedText = selectedText,
            prompt = question
        )
    }

    fun clearAiResponseState() {
        _aiResponseState.value = UiState.Idle
    }
}
