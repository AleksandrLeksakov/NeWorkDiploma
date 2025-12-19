package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName
import ru.netology.nmedia.entity.PostEntity

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

    // Вычисляемое свойство для shares (если есть в API)
    // val shares: Int get() = shareOwnerIds.size

    companion object {
        fun fromEntity(entity: PostEntity): Post = Post(
            id = entity.id,
            authorId = entity.authorId,
            author = entity.author,
            authorJob = entity.authorJob,
            authorAvatar = entity.authorAvatar,
            content = entity.content,
            published = entity.published,
            coordinates = entity.coordinates?.toDto(),
            link = entity.link,
            mentionIds = entity.mentionIds,
            mentionedMe = entity.mentionedMe,
            likeOwnerIds = entity.likeOwnerIds,
            likedByMe = entity.likedByMe,
            attachment = entity.attachment?.toDto(),
            users = emptyMap() // Не храним в БД
        )
    }

    // Метод для проверки, принадлежит ли пост текущему пользователю
    // Будем вычислять в ViewModel на основе authorId и текущего userId
    fun isOwnedByMe(currentUserId: Long?): Boolean {
        return currentUserId != null && authorId == currentUserId
    }
}