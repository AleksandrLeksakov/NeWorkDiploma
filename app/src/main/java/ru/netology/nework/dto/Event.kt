package ru.netology.nework.dto

import java.time.OffsetDateTime

data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: OffsetDateTime,
    val published: OffsetDateTime,
    val coords: Coordinates? = null,
    val type: EventType,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val users: Map<Long, UserPreview> = emptyMap(),
    val ownedByMe: Boolean = false,
) : FeedItem

enum class EventType {
    OFFLINE,
    ONLINE
}