package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharmadipanshu.aistudybuddy.models.AuthState
import com.sharmadipanshu.aistudybuddy.repository.AuthRepository
import com.sharmadipanshu.aistudybuddy.utils.PhoneNumberUtils
import com.sharmadipanshu.aistudybuddy.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData(AuthState())
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        if (!validate(email, password)) return

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                authRepository.login(email.trim(), password)
            }.onSuccess { isVerified ->
                _authState.value = if (isVerified) {
                    AuthState(isAuthenticated = true)
                } else {
                    AuthState(
                        errorMessage = "Please verify your email before logging in. A new verification email has been sent."
                    )
                }
            }.onFailure { throwable ->
                _authState.value = AuthState(errorMessage = throwable.localizedMessage ?: "Login failed")
            }
        }
    }

    fun signup(name: String, phoneNumber: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank()) {
            _authState.value = AuthState(errorMessage = "Please enter your name")
            return
        }

        val normalizedPhone = PhoneNumberUtils.normalizeIndianMobile10(phoneNumber)
        if (normalizedPhone == null) {
            _authState.value = AuthState(errorMessage = "Please enter a valid 10-digit Indian mobile number")
            return
        }
        if (!validate(email, password)) return
        if (password != confirmPassword) {
            _authState.value = AuthState(errorMessage = "Passwords do not match")
            return
        }

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                authRepository.signup(email.trim(), password, name.trim(), normalizedPhone)
            }.onSuccess {
                _authState.value = AuthState(
                    successMessage = "Account created. Please verify your email before logging in.",
                    shouldNavigateToLogin = true
                )
            }.onFailure { throwable ->
                _authState.value = AuthState(errorMessage = throwable.localizedMessage ?: "Sign up failed")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                authRepository.signInWithGoogle(idToken)
            }.onSuccess { outcome ->
                _authState.value = if (outcome.isAuthenticated) {
                    if (outcome.requiresPhoneNumber) {
                        AuthState(shouldNavigateToPhoneNumber = true)
                    } else {
                        AuthState(isAuthenticated = true)
                    }
                } else {
                    AuthState(errorMessage = "Your Google account is not verified yet.")
                }
            }.onFailure { throwable ->
                _authState.value = AuthState(errorMessage = throwable.localizedMessage ?: "Google sign-in failed")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState(errorMessage = "Please enter your email to reset your password")
            return
        }
        if (!ValidationUtils.isValidEmail(email.trim())) {
            _authState.value = AuthState(errorMessage = "Please enter a valid email address")
            return
        }

        _authState.value = AuthState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                authRepository.sendPasswordResetEmail(email.trim())
            }.onSuccess {
                _authState.value = AuthState(successMessage = "Password reset email sent")
            }.onFailure { throwable ->
                _authState.value = AuthState(errorMessage = throwable.localizedMessage ?: "Failed to send reset email")
            }
        }
    }

    fun clearFeedback() {
        _authState.value = _authState.value?.copy(
            errorMessage = null,
            successMessage = null,
            shouldNavigateToPhoneNumber = false,
            shouldNavigateToLogin = false
        )
    }

    private fun validate(email: String, password: String): Boolean {
        when {
            email.isBlank() || password.isBlank() -> {
                _authState.value = AuthState(errorMessage = "Please fill in all required fields")
                return false
            }

            !ValidationUtils.isValidEmail(email.trim()) -> {
                _authState.value = AuthState(errorMessage = "Please enter a valid email address")
                return false
            }

            !ValidationUtils.isValidPassword(password) -> {
                _authState.value = AuthState(errorMessage = "Password must be at least 6 characters")
                return false
            }
        }
        return true
    }
}
