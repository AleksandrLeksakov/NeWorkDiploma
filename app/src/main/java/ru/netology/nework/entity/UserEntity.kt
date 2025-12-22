package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserEntity")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?
) {
    fun toDto() = ru.netology.nework.dto.UserResponse(id, login, name, avatar)

    companion object {
        fun fromDto(dto: ru.netology.nework.dto.UserResponse) = UserEntity(
            id = dto.id,
            login = dto.login,
            name = dto.name,
            avatar = dto.avatar
        )
    }
}