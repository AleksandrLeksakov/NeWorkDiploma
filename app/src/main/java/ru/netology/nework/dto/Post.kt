package ru.netology.nework.dto

import java.time.OffsetDateTime

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val published: OffsetDateTime,
    val coords: Coordinates? = null,
    val link: String? = null,
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val users: Map<Long, UserPreview> = emptyMap(),
    val ownedByMe: Boolean = false,
) : FeedItem