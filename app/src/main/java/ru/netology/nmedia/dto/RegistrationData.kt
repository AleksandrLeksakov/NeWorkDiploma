package ru.netology.nmedia.dto

data class RegistrationData (
    val login: String,
    val password: String,
    val name: String,
    val avatar: String? = null
)
