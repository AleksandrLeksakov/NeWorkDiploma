package ru.netology.nework.model

data class JobModel(
    val name: String,
    val position: String,
    val start: String,
    val finish: String? = null,
    val link: String? = null
)
