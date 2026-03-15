package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.ApiResult
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

    fun fetchNotes() {
        _notesState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = studyRepository.fetchNotes()) {
                is ApiResult.Success -> _notesState.value = UiState.Success(result.data)
                is ApiResult.Error -> _notesState.value = UiState.Error(result.message)
            }
        }
    }
}
