package ru.netology.nework.entity

import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType

data class AttachmentEmbeddable(
    val url: String,
    val type: AttachmentType
) {
    fun toDto() = Attachment(url, type)


}