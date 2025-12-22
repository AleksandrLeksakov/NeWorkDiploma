package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Job

@Entity(tableName = "JobEntity")
data class JobEntity(
    @PrimaryKey
    val id: Long,
    val userId: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
) {
    fun toDto() = Job(id, name, position, start, finish, link)

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