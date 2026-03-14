package com.sharmadipanshu.aistudybuddy.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean = password.length >= 6
}
