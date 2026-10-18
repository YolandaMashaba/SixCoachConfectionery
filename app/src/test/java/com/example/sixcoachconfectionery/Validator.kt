package com.example.sixcoachconfectionery

object Validator {
    fun isValidEmail(email: String): Boolean =
        email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))

    fun isValidPassword(password: String): Boolean = password.length >= 8

    fun isValidQuantity(qty: Int): Boolean = qty in 1..999
}