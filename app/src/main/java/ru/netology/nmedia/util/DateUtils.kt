package ru.netology.nmedia.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val displayFormatter = DateTimeFormatter
        .ofPattern("dd.MM.yyyy HH:mm")
        .withLocale(Locale.getDefault())

    /**
     * Форматирует ISO дату для отображения
     * Пример: "2025-12-08T11:42:13.187Z" → "08.12.2025 11:42"
     */
    fun formatIsoForDisplay(isoDate: String): String {
        return try {
            val instant = Instant.parse(isoDate)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            dateTime.format(displayFormatter)
        } catch (e: Exception) {
            // Fallback: если не ISO формат
            isoDate
        }
    }

    /**
     * Создает текущее время в ISO формате для новых постов
     */
    fun currentTimeIso(): String {
        return Instant.now().toString()
    }

    /**
     * Проверяет является ли строка валидной ISO датой
     */
    fun isValidIsoDate(dateStr: String): Boolean {
        return try {
            Instant.parse(dateStr)
            true
        } catch (e: Exception) {
            false
        }
    }
}