package ru.netology.nmedia.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
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

    @ColumnInfo(name = "author_avatar")
    val authorAvatar: String?,

    @ColumnInfo(name = "author_job")
    val authorJob: String?,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "published")
    val published: String,  // Или Long

    @ColumnInfo(name = "liked_by_me")
    val likedByMe: Boolean,

    @ColumnInfo(name = "likes")
    val likes: Int,

    @ColumnInfo(name = "like_owner_ids")
    val likeOwnerIds: List<Long>,

    @ColumnInfo(name = "mentioned_me")
    val mentionedMe: Boolean,

    @ColumnInfo(name = "mentioned_ids")
    val mentionedIds: List<Long>,

    @Embedded
    val attachment: AttachmentEmbeddable?,

    @ColumnInfo(name = "link")
    val link: String?,

    @ColumnInfo(name = "owned_by_me")
    val ownedByMe: Boolean,

    @Embedded
    val coords: CoordinatesEmbeddable?
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar ?: "",
        authorJob = authorJob,
        content = content,
        published = published,
        likedByMe = likedByMe,
        likes = likes,
        likeOwnerIds = likeOwnerIds,
        mentionedMe = mentionedMe,
        mentionedIds = mentionedIds,
        attachment = attachment?.toDto(),
        link = link,
        ownedByMe = ownedByMe,
        coords = coords?.toDto()
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            id = dto.id,
            authorId = dto.authorId,
            author = dto.author,
            authorAvatar = dto.authorAvatar,
            authorJob = dto.authorJob,
            content = dto.content,
            published = dto.published,
            likedByMe = dto.likedByMe,
            likes = dto.likes,
            likeOwnerIds = dto.likeOwnerIds,
            mentionedMe = dto.mentionedMe,
            mentionedIds = dto.mentionedIds,
            attachment = dto.attachment?.let { AttachmentEmbeddable.fromDto(it) },
            link = dto.link,
            ownedByMe = dto.ownedByMe,
            coords = dto.coords?.let { CoordinatesEmbeddable.fromDto(it) }
        )
    }
}