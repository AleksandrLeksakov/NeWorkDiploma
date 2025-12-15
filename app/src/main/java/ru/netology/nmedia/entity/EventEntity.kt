package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.EventType
import ru.netology.nmedia.enumeration.AttachmentType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val coordinates: CoordinatesEmbeddable?,
    val link: String?,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean,
    val attachment: AttachmentEmbeddable?,
    val type: String, // Храним как String
) {
    // Конвертация в EventType при запросе
    val eventType: EventType
        get() = EventType.valueOf(type.uppercase())
}