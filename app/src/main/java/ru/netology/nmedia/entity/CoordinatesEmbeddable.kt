package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Coordinates

data class CoordinatesEmbeddable(
    val lat: String,
    val long: String
) {
    fun toDto() = Coordinates(lat, long)


}