package ru.netology.nmedia.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatPublishedDate(published: String): String {
    return try {
        val instant = Instant.parse(published)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        published
    }
}

fun getLikeText(likes: Int, likedByMe: Boolean): String {
    return when {
        likes > 1000 -> String.format("%.1fK", likes / 1000.0)
        likes > 0 -> likes.toString()
        else -> ""
    }
}