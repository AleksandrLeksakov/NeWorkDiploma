package ru.netology.nework.entity.event

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.entity.KeyType

@Entity
data class EventRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long
)
