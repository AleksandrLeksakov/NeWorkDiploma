package ru.netology.nmedia.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "posts_remote_keys",
    primaryKeys = ["type", "id"]
)
data class PostRemoteKeyEntity(
    @ColumnInfo(name = "type")
    val type: KeyType,
    @ColumnInfo(name = "id")
    val id: Long
) {
    enum class KeyType {
        AFTER, BEFORE
    }

    companion object {
        const val TYPE_AFTER = "AFTER"
        const val TYPE_BEFORE = "BEFORE"
    }
}