package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    enum class StartDestination {
        LOGIN,
        PHONE_SETUP,
        MAIN
    }

    private val _startDestination = MutableLiveData<StartDestination>()
    val startDestination: LiveData<StartDestination> = _startDestination

    fun resolveStartDestination() {
        if (!authRepository.isUserLoggedIn()) {
            _startDestination.value = StartDestination.LOGIN
            return
        }

        viewModelScope.launch {
            val shouldCollectPhone =
                authRepository.isCurrentUserGoogleUser() && authRepository.isCurrentUserPhoneMissing()

            _startDestination.value = if (shouldCollectPhone) {
                StartDestination.PHONE_SETUP
            } else {
                StartDestination.MAIN
            }
        }
    }
}
