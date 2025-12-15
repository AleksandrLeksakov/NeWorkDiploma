package ru.netology.nmedia.dto
import com.google.gson.annotations.SerializedName

data class Coordinates(
    @SerializedName("lat")
    val lat: String,

    @SerializedName("long")
    val long: String
)