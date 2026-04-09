package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.CalendarEvent
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.StudyEvent
import com.sharmadipanshu.aistudybuddy.models.StudyEventRequest
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _events = MutableLiveData<List<CalendarEvent>>(emptyList())
    val events: LiveData<List<CalendarEvent>> = _events

    private val _eventState = MutableLiveData<UiState<String>>(UiState.Idle)
    val eventState: LiveData<UiState<String>> = _eventState

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            when (val result = studyRepository.fetchEvents()) {
                is ApiResult.Success -> {
                    _events.value = if (result.data.isNotEmpty()) {
                        result.data.map { it.toCalendarEvent() }
                    } else {
                        fallbackEvents()
                    }
                }
                is ApiResult.Error -> _events.value = fallbackEvents()
            }
        }
    }

    fun createEvent(title: String, description: String, date: String, time: String) {
        if (title.isBlank() || date.isBlank() || time.isBlank()) {
            _eventState.value = UiState.Error("Title, date, and time are required.")
            return
        }

        _eventState.value = UiState.Loading
        viewModelScope.launch {
            when (
                val result = studyRepository.createEvent(
                    StudyEventRequest(
                        title = title.trim(),
                        description = description.trim(),
                        date = date.trim(),
                        time = time.trim()
                    )
                )
            ) {
                is ApiResult.Success -> {
                    _eventState.value = UiState.Success("Study session added to your planner.")
                    _events.value = listOf(result.data.toCalendarEvent()) + (_events.value ?: emptyList())
                }
                is ApiResult.Error -> _eventState.value = UiState.Error(result.message)
            }
        }
    }

    fun clearEventState() {
        _eventState.value = UiState.Idle
    }

    private fun fallbackEvents(): List<CalendarEvent> = listOf(
        CalendarEvent("Physics revision block", "08:00 AM - 09:30 AM", "Kinematics problem solving"),
        CalendarEvent("Collaborative study room", "04:00 PM - 05:00 PM", "Database systems with teammates"),
        CalendarEvent("AI quiz recap", "08:30 PM - 09:00 PM", "Adaptive recall session")
    )

    private fun StudyEvent.toCalendarEvent(): CalendarEvent =
        CalendarEvent(
            title = title,
            time = "$date • $time",
            description = description.ifBlank { "Planned study session" }
        )
}
