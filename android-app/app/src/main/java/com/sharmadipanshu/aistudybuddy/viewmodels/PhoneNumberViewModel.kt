package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.PhoneNumberState
import com.sharmadipanshu.aistudybuddy.repository.UserRepository
import com.sharmadipanshu.aistudybuddy.utils.PhoneNumberUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneNumberViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableLiveData(PhoneNumberState())
    val state: LiveData<PhoneNumberState> = _state

    fun savePhoneNumber(phoneNumber: String) {
        val normalized = PhoneNumberUtils.normalizeIndianMobile10(phoneNumber)
        if (normalized == null) {
            _state.value =
                PhoneNumberState(errorMessage = "Please enter a valid 10-digit Indian mobile number")
            return
        }

        _state.value = PhoneNumberState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                userRepository.updatePhoneNumber(normalized)
            }.onSuccess {
                _state.value = PhoneNumberState(
                    successMessage = "Phone number saved",
                    shouldNavigateHome = true
                )
            }.onFailure { throwable ->
                _state.value = PhoneNumberState(
                    errorMessage = throwable.localizedMessage ?: "Failed to save phone number"
                )
            }
        }
    }

    fun clearFeedback() {
        _state.value = _state.value?.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
