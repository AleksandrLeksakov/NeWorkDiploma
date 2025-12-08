package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id")
    val id: Long,

    @SerializedName("authorId")
    val authorId: Long,

    @SerializedName("author")
    val author: String,

    @SerializedName("authorAvatar")
    val authorAvatar: String? = null,

    @SerializedName("authorJob")
    val authorJob: String? = null,

    @SerializedName("content")
    val content: String,

    @SerializedName("published")
    val published: String,

    @SerializedName("coords")
    val coords: Coordinates? = null,

    @SerializedName("link")
    val link: String? = null,

    @SerializedName("mentionIds")
    val mentionIds: List<Long> = emptyList(),

    @SerializedName("mentionedMe")
    val mentionedMe: Boolean = false,

    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Long> = emptyList(),

    @SerializedName("likedByMe")
    val likedByMe: Boolean,

    val likes: Int = likeOwnerIds.size,

    @SerializedName("attachment")
    val attachment: Attachment? = null,

    val ownedByMe: Boolean = false

    // УБРАТЬ: val users: Map<Long, UserPreview> = emptyMap()
)