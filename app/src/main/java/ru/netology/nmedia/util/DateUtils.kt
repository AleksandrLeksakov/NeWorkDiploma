package ru.netology.nmedia.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val displayFormatter = DateTimeFormatter
        .ofPattern("dd.MM.yyyy HH:mm")
        .withLocale(Locale.getDefault())

    // Для ISO формата (2024-01-15T10:30:00Z)
    fun formatIsoForDisplay(isoDate: String): String {
        return try {
            val instant = Instant.parse(isoDate)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            dateTime.format(displayFormatter)
        } catch (e: Exception) {
            // Если не ISO, попробуем как timestamp
            try {
                val timestamp = isoDate.toLong()
                val instant = Instant.ofEpochMilli(timestamp)
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                dateTime.format(displayFormatter)
            } catch (e2: Exception) {
                isoDate // fallback
            }
        }
    }

    // Для timestamp (Long)
    fun formatTimestampForDisplay(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return dateTime.format(displayFormatter)
    }

    fun currentTimeIso(): String {
        return Instant.now().toString()
    }

    fun currentTimeTimestamp(): Long {
        return System.currentTimeMillis()
    }
}