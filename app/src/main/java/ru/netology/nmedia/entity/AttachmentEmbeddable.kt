package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.enumeration.AttachmentType

data class AttachmentEmbeddable(
    val url: String,
    val type: AttachmentType
) {
    fun toDto() = Attachment(url, type)


}