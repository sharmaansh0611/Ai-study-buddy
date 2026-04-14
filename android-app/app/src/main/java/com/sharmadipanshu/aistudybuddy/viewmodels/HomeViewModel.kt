package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.R
import com.sharmadipanshu.aistudybuddy.models.ApiResult
import com.sharmadipanshu.aistudybuddy.models.DashboardItem
import com.sharmadipanshu.aistudybuddy.repository.StudyRepository
import com.sharmadipanshu.aistudybuddy.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _dashboardItems = MutableLiveData(
        listOf(
            DashboardItem("Upload Notes", "Sync PDFs and handwritten notes", R.drawable.ic_upload_24),
            DashboardItem("My Notes", "Browse your revision library", R.drawable.ic_notes_24),
            DashboardItem("Ask AI", "Get instant concept explanations", R.drawable.ic_spark_24),
            DashboardItem("Study Rooms", "Collaborate with peers live", R.drawable.ic_groups_24),
            DashboardItem("Quizzes", "Practice with smart recall", R.drawable.ic_quiz_24),
            DashboardItem("Calendar", "Track your study plan", R.drawable.ic_calendar_24)
        )
    )
    val dashboardItems: LiveData<List<DashboardItem>> = _dashboardItems

    private val _apiStatus = MutableLiveData("Connecting to your learning workspace...")
    val apiStatus: LiveData<String> = _apiStatus

    private val _userName = MutableLiveData("Learner")
    val userName: LiveData<String> = _userName

    fun loadUserName() {
        viewModelScope.launch {
            runCatching {
                userRepository.getResolvedCurrentUserName()
            }.onSuccess { name ->
                if (name.isNotBlank()) {
                    _userName.value = name
                }
            }
        }
    }

    fun refreshApiStatus() {
        viewModelScope.launch {
            when (val result = studyRepository.fetchApiStatus()) {
                is ApiResult.Success -> _apiStatus.value = result.data
                is ApiResult.Error -> _apiStatus.value = result.message
            }
        }
    }
}
