package ru.netology.nework.entity

import ru.netology.nework.dto.Coordinates

data class CoordinatesEmbeddable(
    val lat: Double,
    val long: Double
) {
    fun toDto() = Coordinates(lat, long)


}