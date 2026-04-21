package com.sharmadipanshu.aistudybuddy.models

data class PhoneNumberState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val shouldNavigateHome: Boolean = false
)
