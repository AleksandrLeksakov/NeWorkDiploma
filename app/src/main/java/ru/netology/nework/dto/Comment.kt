package ru.netology.nework.dto

import java.time.OffsetDateTime

data class Comment(
    val id: Long,
    val postId: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: OffsetDateTime,
    val likeOwnerIds: List<Long>
)
