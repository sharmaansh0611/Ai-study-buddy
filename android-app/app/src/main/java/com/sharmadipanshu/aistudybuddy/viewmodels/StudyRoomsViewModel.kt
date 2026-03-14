package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.StudyRoom
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyRoomsViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _roomsState = MutableLiveData<UiState<List<StudyRoom>>>(UiState.Loading)
    val roomsState: LiveData<UiState<List<StudyRoom>>> = _roomsState

    fun fetchStudyRooms() {
        viewModelScope.launch {
            _roomsState.value = UiState.Loading
            when (val result = studyRepository.fetchStudyRooms()) {
                is ApiResult.Success -> {
                    val rooms = if (result.data.isNotEmpty()) {
                        result.data
                    } else {
                        fallbackRooms()
                    }
                    _roomsState.value = UiState.Success(rooms)
                }

                is ApiResult.Error -> _roomsState.value = UiState.Success(fallbackRooms())
            }
        }
    }

    private fun fallbackRooms(): List<StudyRoom> = listOf(
        StudyRoom("1", "Organic Chemistry Sprint", "Reaction mechanisms", 12, "Today • 7:00 PM"),
        StudyRoom("2", "DSA Interview Prep", "Graphs and dynamic programming", 18, "Tomorrow • 6:30 PM"),
        StudyRoom("3", "Calculus Crash Session", "Integrals and applications", 9, "Sat • 9:00 AM")
    )
}
