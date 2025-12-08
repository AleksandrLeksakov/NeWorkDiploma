package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType
import com.google.gson.annotations.SerializedName

data class Attachment(
    @SerializedName("url")
    val url: String,

    @SerializedName("type")
    val type: AttachmentType,

    @SerializedName("description")
    val description: String? = null
)