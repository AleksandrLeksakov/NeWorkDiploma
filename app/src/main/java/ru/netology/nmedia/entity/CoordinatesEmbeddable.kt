package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Coordinates

data class CoordinatesEmbeddable(
    val lat: Double,
    val long: Double
) {
    fun toDto() = Coordinates(lat, long)


}