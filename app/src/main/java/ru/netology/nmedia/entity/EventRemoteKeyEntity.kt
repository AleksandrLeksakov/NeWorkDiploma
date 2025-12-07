package ru.netology.nmedia.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import ru.netology.nmedia.dto.RemoteKeyType

@Entity(
    tableName = "events_remote_keys",
    primaryKeys = ["type", "event_id"]
)
data class EventRemoteKeyEntity(
    @ColumnInfo(name = "type")
    val type: RemoteKeyType,
    @ColumnInfo(name = "event_id")
    val eventId: Long
)