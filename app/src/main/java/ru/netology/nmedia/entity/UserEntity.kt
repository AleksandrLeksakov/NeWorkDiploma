package ru.netology.nmedia.entity

import androidx.room.*
import ru.netology.nmedia.dto.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "login")
    val login: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "avatar")
    val avatar: String?,
    @ColumnInfo(name = "lat")
    val lat: Double?,
    @ColumnInfo(name = "lng")
    val lng: Double?,
    @ColumnInfo(name = "last_seen")
    val lastSeen: String?
) {
    fun toDto() = User(
        id = id,
        login = login,
        name = name,
        avatar = avatar,
        lat = lat,
        lng = lng,
        lastSeen = lastSeen
    )

    companion object {
        fun fromDto(dto: User) = UserEntity(
            id = dto.id,
            login = dto.login,
            name = dto.name,
            avatar = dto.avatar,
            lat = dto.lat,
            lng = dto.lng,
            lastSeen = dto.lastSeen
        )
    }
}