package ru.netology.nmedia.entity

import androidx.room.ColumnInfo
import ru.netology.nmedia.dto.Coordinates

data class CoordinatesEmbeddable(
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "lng")
    val lng: Double
) {
    fun toDto() = Coordinates(lat, lng)

    companion object {
        fun fromDto(dto: Coordinates) = CoordinatesEmbeddable(dto.lat, dto.lng)
    }
}
