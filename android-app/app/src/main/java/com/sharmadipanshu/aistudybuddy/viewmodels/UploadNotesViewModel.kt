package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.UploadNotesResponse
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadNotesViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _uploadState = MutableLiveData<UiState<UploadNotesResponse>>(UiState.Idle)
    val uploadState: LiveData<UiState<UploadNotesResponse>> = _uploadState

    fun uploadNote(title: String, pdfFile: File?) {
        when {
            title.isBlank() -> {
                _uploadState.value = UiState.Error("Please enter a note title.")
                return
            }

            pdfFile == null -> {
                _uploadState.value = UiState.Error("Please choose a PDF file.")
                return
            }
        }

        _uploadState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.uploadNotes(title.trim(), pdfFile)) {
                is ApiResult.Success -> _uploadState.value = UiState.Success(result.data)
                is ApiResult.Error -> _uploadState.value = UiState.Error(result.message)
            }
        }
    }

    fun clearState() {
        _uploadState.value = UiState.Idle
    }
}
