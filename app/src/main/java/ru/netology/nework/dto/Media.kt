package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class Media(
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String
)

data class MediaUpload(
    val file: java.io.File
)