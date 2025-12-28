package ru.netology.nework.dto

import java.time.OffsetDateTime

data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val start: OffsetDateTime,
    val finish: OffsetDateTime? = null,  // Теперь nullable
    val link: String? = null
)