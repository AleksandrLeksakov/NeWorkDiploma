package ru.netology.nmedia.dto

data class AuthState(
    val id: Long = 0,
    val token: String? = null,
    val login: String? = null,
    val name: String? = null,
    val avatar: String? = null
)