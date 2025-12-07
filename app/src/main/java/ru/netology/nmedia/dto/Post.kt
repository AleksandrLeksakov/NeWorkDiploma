package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val authorJob: String?,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val likeOwnerIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val mentionedIds: List<Long> = emptyList(),
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val coords: Coordinates? = null,
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
    val description: String? = null
)
