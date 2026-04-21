package com.sharmadipanshu.aistudybuddy.utils

object PhoneNumberUtils {

    private val indianMobileRegex = Regex("^[6-9]\\d{9}$")

    fun normalizeIndianMobile10(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        if (digits.length != 10) return null
        if (!indianMobileRegex.matches(digits)) return null
        return digits
    }
}

