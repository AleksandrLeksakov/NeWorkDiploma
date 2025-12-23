package ru.netology.nework.entity.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.entity.keytype.KeyType

@Entity(tableName = "postKey")
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long
)