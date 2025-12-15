package ru.netology.nmedia.entity

import androidx.room.*
import ru.netology.nmedia.dto.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "author_id")
    val authorId: Long,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "author_job")
    val authorJob: String?,

    @ColumnInfo(name = "author_avatar")
    val authorAvatar: String?,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "published")
    val published: String,

    @Embedded
    val coordinates: CoordinatesEmbeddable?,

    @ColumnInfo(name = "link")
    val link: String?,

    @ColumnInfo(name = "mention_ids")
    val mentionIds: List<Long>,

    @ColumnInfo(name = "mentioned_me")
    val mentionedMe: Boolean,

    @ColumnInfo(name = "like_owner_ids")
    val likeOwnerIds: List<Long>,

    @ColumnInfo(name = "liked_by_me")
    val likedByMe: Boolean,

    @Embedded
    val attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        coordinates = coordinates?.toDto(),
        link = link,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        attachment = attachment?.toDto(),
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            id = dto.id,
            authorId = dto.authorId,
            author = dto.author,
            authorJob = dto.authorJob,
            authorAvatar = dto.authorAvatar,
            content = dto.content,
            published = dto.published,
            coordinates = dto.coordinates?.let { CoordinatesEmbeddable(it.lat, it.long) }, // ПРОСТОЕ СОЗДАНИЕ
            link = dto.link,
            mentionIds = dto.mentionIds,
            mentionedMe = dto.mentionedMe,
            likeOwnerIds = dto.likeOwnerIds,
            likedByMe = dto.likedByMe,
            attachment = dto.attachment?.let { AttachmentEmbeddable(it.url, it.type) }, // ПРОСТОЕ СОЗДАНИЕ
        )
    }
}