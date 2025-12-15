package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName

data class Post(
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

    @SerializedName("published")
    val published: String,

    @SerializedName("coordinates")
    val coordinates: Coordinates? = null,

    @SerializedName("link")
    val link: String? = null,

    @SerializedName("mentionIds")
    val mentionIds: List<Long> = emptyList(),

    @SerializedName("mentionedMe")
    val mentionedMe: Boolean = false,

    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Long> = emptyList(),

    @SerializedName("likedByMe")
    val likedByMe: Boolean = false,

    @SerializedName("attachment")
    val attachment: Attachment? = null,

    @SerializedName("users")
    val users: Map<Long, UserPreview> = emptyMap(),
) {
    // Вычисляемое свойство для количества лайков
    val likes: Int get() = likeOwnerIds.size

    // Вычисляемое свойство для определения владельца (не из API!)
    // Это будет вычисляться на клиенте, а не приходить с сервера
    // Удалите ownedByMe из DTO, так как его нет в API
}