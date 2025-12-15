package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?
)
