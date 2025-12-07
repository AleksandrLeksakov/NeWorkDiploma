package ru.netology.nmedia.entity

import androidx.room.*
import ru.netology.nmedia.dto.Job

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "position")
    val position: String,
    @ColumnInfo(name = "start")
    val start: String,
    @ColumnInfo(name = "finish")
    val finish: String?,
    @ColumnInfo(name = "link")
    val link: String?
) {
    fun toDto() = Job(
        id = id,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link
    )

    companion object {
        fun fromDto(dto: Job, userId: Long) = JobEntity(
            id = dto.id,
            userId = userId,
            name = dto.name,
            position = dto.position,
            start = dto.start,
            finish = dto.finish,
            link = dto.link
        )
    }
}
