package com.sharmadipanshu.aistudybuddy.models

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val shouldNavigateToPhoneNumber: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val shouldNavigateToLogin: Boolean = false
)
