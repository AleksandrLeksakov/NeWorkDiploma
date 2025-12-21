package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName


data class AuthResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("token")
    val token: String,

    @SerializedName("login")
    val login: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null
)
