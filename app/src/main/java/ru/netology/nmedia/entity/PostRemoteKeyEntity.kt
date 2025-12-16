package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class PostRemoteKeyEntity(
    @PrimaryKey
    val id: Long,
    val nextKey: Long?,
    val type: String = "POST"
)