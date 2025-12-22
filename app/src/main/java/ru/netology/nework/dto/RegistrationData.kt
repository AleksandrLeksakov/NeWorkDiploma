package ru.netology.nework.dto

data class RegistrationData (
    val login: String,
    val password: String,
    val name: String,
    val avatar: String? = null
)
