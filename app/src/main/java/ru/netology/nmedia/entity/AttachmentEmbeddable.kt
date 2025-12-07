package ru.netology.nmedia.entity

import androidx.room.ColumnInfo
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.enumeration.AttachmentType

data class AttachmentEmbeddable(
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "type")
    val type: AttachmentType,
    @ColumnInfo(name = "description")
    val description: String? = null
) {
    fun toDto() = Attachment(url, type, description)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type, it.description)
        }
    }
}