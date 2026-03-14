package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sharmadipanshu.aistudybuddy.models.CalendarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableLiveData(
        listOf(
            CalendarEvent("Physics revision block", "08:00 AM - 09:30 AM", "Kinematics problem solving"),
            CalendarEvent("Collaborative study room", "04:00 PM - 05:00 PM", "Database systems with teammates"),
            CalendarEvent("AI quiz recap", "08:30 PM - 09:00 PM", "Adaptive recall session")
        )
    )
    val events: LiveData<List<CalendarEvent>> = _events
}
