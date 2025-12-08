package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName

data class UserPreview(
    @SerializedName("name")
    val name: String,

    @SerializedName("avatar")
    val avatar: String?
)