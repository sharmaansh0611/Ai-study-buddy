package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.User
import com.sharmadipanshu.aistudybuddy.repository.UserRepository
import com.sharmadipanshu.aistudybuddy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfileState = MutableLiveData<UiState<User>>(UiState.Idle)
    val userProfileState: LiveData<UiState<User>> = _userProfileState

    private val _userExistsState = MutableLiveData<UiState<Boolean>>(UiState.Idle)
    val userExistsState: LiveData<UiState<Boolean>> = _userExistsState

    fun createUserProfile(nameOverride: String? = null) {
        _userProfileState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                userRepository.createUserProfile(nameOverride)
            }.onSuccess { user ->
                _userProfileState.value = UiState.Success(user)
            }.onFailure { throwable ->
                _userProfileState.value = UiState.Error(
                    throwable.localizedMessage ?: "Unable to create user profile."
                )
            }
        }
    }

    fun getCurrentUserProfile() {
        _userProfileState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                userRepository.getUserProfile()
                    ?: throw IllegalStateException("User profile not found.")
            }.onSuccess { user ->
                _userProfileState.value = UiState.Success(user)
            }.onFailure { throwable ->
                _userProfileState.value = UiState.Error(
                    throwable.localizedMessage ?: "Unable to load user profile."
                )
            }
        }
    }

    fun checkUserExists() {
        _userExistsState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                userRepository.checkUserExists()
            }.onSuccess { exists ->
                _userExistsState.value = UiState.Success(exists)
            }.onFailure { throwable ->
                _userExistsState.value = UiState.Error(
                    throwable.localizedMessage ?: "Unable to check user profile."
                )
            }
        }
    }
}
