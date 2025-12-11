package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val id: Long,

    @SerializedName("authorId")
    val authorId: Long,

    @SerializedName("author")
    val author: String,

    @SerializedName("authorJob")
    val authorJob: String? = null,

    @SerializedName("authorAvatar")
    val authorAvatar: String? = null,

    @SerializedName("content")
    val content: String,

    @SerializedName("datetime")
    val datetime: String,

    @SerializedName("published")
    val published: String,

    @SerializedName("coords")
    val coords: Coordinates? = null,

    @SerializedName("type")
    val type: String, // "ONLINE" или "OFFLINE"

    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Long> = emptyList(),

    @SerializedName("likedByMe")
    val likedByMe: Boolean = false,

    @SerializedName("speakerIds")
    val speakerIds: List<Long> = emptyList(),

    @SerializedName("participantsIds")
    val participantsIds: List<Long> = emptyList(),

    @SerializedName("participatedByMe")
    val participatedByMe: Boolean = false,

    @SerializedName("attachment")
    val attachment: Attachment? = null,

    @SerializedName("link")
    val link: String? = null,

    @SerializedName("users")
    val users: Map<Long, UserPreview> = emptyMap(),
) {
    // ВЫЧИСЛЯЕМЫЕ свойства
    val likes: Int get() = likeOwnerIds.size
    val participants: Int get() = participantsIds.size
    val speakers: Int get() = speakerIds.size
}