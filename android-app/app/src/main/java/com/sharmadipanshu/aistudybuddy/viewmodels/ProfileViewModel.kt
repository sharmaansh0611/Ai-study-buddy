package com.sharmadipanshu.aistudybuddy.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sharmadipanshu.aistudybuddy.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userEmail = MutableLiveData(authRepository.getCurrentUserEmail())
    val userEmail: LiveData<String> = _userEmail

    fun signOut() {
        authRepository.signOut()
    }
}
