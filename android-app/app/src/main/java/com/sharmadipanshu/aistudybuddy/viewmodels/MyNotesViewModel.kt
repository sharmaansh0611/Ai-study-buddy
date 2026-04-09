package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.DeleteNoteResponse
import com.sharmadipanshu.aistudybuddy.models.Note
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyNotesViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _notesState = MutableLiveData<UiState<List<Note>>>(UiState.Loading)
    val notesState: LiveData<UiState<List<Note>>> = _notesState
    private val _deleteState = MutableLiveData<UiState<DeleteNoteResponse>>(UiState.Idle)
    val deleteState: LiveData<UiState<DeleteNoteResponse>> = _deleteState
    private var currentNotes: List<Note> = emptyList()

    fun fetchNotes() {
        _notesState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.fetchNotes()) {
                is ApiResult.Success -> {
                    currentNotes = result.data
                    _notesState.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> _notesState.value = UiState.Error(result.message)
            }
        }
    }

    fun deleteNote(noteId: String) {
        _deleteState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.deleteNote(noteId)) {
                is ApiResult.Success -> {
                    currentNotes = currentNotes.filterNot { it.noteId == noteId }
                    _notesState.value = UiState.Success(currentNotes)
                    _deleteState.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> _deleteState.value = UiState.Error(result.message)
            }
        }
    }

    fun clearDeleteState() {
        _deleteState.value = UiState.Idle
    }
}
