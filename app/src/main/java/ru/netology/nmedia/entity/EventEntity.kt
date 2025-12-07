package ru.netology.nmedia.entity

import androidx.room.*
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.EventType
import ru.netology.nmedia.dto.Coordinates
import ru.netology.nmedia.dto.Attachment

@Entity(tableName = "events")
data class EventEntity(
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
    @ColumnInfo(name = "datetime")
    val datetime: String,
    @ColumnInfo(name = "published")
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    @ColumnInfo(name = "type")
    val type: EventType,
    @ColumnInfo(name = "like_owner_ids")
    val likeOwnerIds: List<Long>,
    @ColumnInfo(name = "liked_by_me")
    val likedByMe: Boolean,
    @ColumnInfo(name = "speaker_ids")
    val speakerIds: List<Long>,
    @ColumnInfo(name = "participants_ids")
    val participantsIds: List<Long>,
    @ColumnInfo(name = "participated_by_me")
    val participatedByMe: Boolean,
    @Embedded
    val attachment: AttachmentEmbeddable?,
    @ColumnInfo(name = "link")
    val link: String?
) {
    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        datetime = datetime,
        published = published,
        coords = coords?.toDto(),
        type = type,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = attachment?.toDto(),
        link = link,
        users = emptyMap()
    )

    companion object {
        fun fromDto(dto: Event) = EventEntity(
            id = dto.id,
            authorId = dto.authorId,
            author = dto.author,
            authorAvatar = dto.authorAvatar,
            authorJob = dto.authorJob,
            content = dto.content,
            datetime = dto.datetime,
            published = dto.published,
            coords = dto.coords?.let { CoordinatesEmbeddable.fromDto(it) },
            type = dto.type,
            likeOwnerIds = dto.likeOwnerIds,
            likedByMe = dto.likedByMe,
            speakerIds = dto.speakerIds,
            participantsIds = dto.participantsIds,
            participatedByMe = dto.participatedByMe,
            attachment = dto.attachment?.let { AttachmentEmbeddable.fromDto(it) },
            link = dto.link
        )
    }
}
