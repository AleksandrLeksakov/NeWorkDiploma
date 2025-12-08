package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Coordinates
import androidx.room.ColumnInfo


data class CoordinatesEmbeddable(
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "long")  // "long" в базе
    val long: Double  // long вместо lng
) {
    fun toDto() = Coordinates(lat, long)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.lat, it.long)
        }
    }
}