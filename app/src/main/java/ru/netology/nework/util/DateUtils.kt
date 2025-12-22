package ru.netology.nework.util

import ru.netology.nework.dto.Job
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {
    // Для постов и событий (публикация) - ТЗ: "dd.mm.yyyy HH:mm"
    private val postFormatter = DateTimeFormatter
        .ofPattern("dd.MM.yyyy HH:mm")
        .withLocale(Locale.getDefault())

    // Для событий (дата проведения) - ТЗ: "dd.mm.yyyy HH:mm"
    // Можно использовать тот же форматтер

    // Для работ - ТЗ: "dd MMM yyyy"
    private val jobPeriodFormatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy")
        .withLocale(Locale.getDefault())

    /**
     * Форматирует ISO дату для отображения постов/событий
     * Соответствует ТЗ: "дата публикации в формате dd.mm.yyyy HH:mm"
     */
    fun formatPostDate(isoDate: String): String {
        return try {
            val instant = Instant.parse(isoDate)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            dateTime.format(postFormatter)
        } catch (e: Exception) {
            isoDate // fallback
        }
    }

    /**
     * Форматирует период работы для Job (пункт 6.2 ТЗ)
     * "стаж в формате dd MMM yyyy"
     */
    fun formatJobPeriod(job: Job): String {
        return try {
            val startInstant = Instant.parse(job.start)
            val startDate = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault())
            val startFormatted = startDate.format(jobPeriodFormatter)

            if (job.finish != null && job.finish.isNotBlank()) {
                val finishInstant = Instant.parse(job.finish)
                val finishDate = LocalDateTime.ofInstant(finishInstant, ZoneId.systemDefault())
                val finishFormatted = finishDate.format(jobPeriodFormatter)
                "$startFormatted - $finishFormatted"
            } else {
                val currentText = when (Locale.getDefault().language) {
                    "ru" -> "настоящее время"
                    else -> "present"
                }
                "$startFormatted - $currentText"
            }
        } catch (e: Exception) {
            "${job.start.take(10)} - ${job.finish?.take(10) ?: "present"}"
        }
    }

    /**
     * Для удобства - прямая работа со строками ISO
     */
    fun formatIsoToJobDate(isoDate: String): String {
        return try {
            val instant = Instant.parse(isoDate)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            dateTime.format(jobPeriodFormatter)
        } catch (e: Exception) {
            isoDate.take(10)
        }
    }

    /**
     * ДОПОЛНИТЕЛЬНО: метод для форматирования только даты начала работы
     */
    fun formatJobStart(job: Job): String {
        return try {
            val instant = Instant.parse(job.start)
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            dateTime.format(jobPeriodFormatter)
        } catch (e: Exception) {
            job.start.take(10)
        }
    }

    /**
     * ДОПОЛНИТЕЛЬНО: метод для форматирования только даты окончания работы
     */
    fun formatJobFinish(job: Job): String {
        return job.finish?.let { finish ->
            try {
                val instant = Instant.parse(finish)
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                dateTime.format(jobPeriodFormatter)
            } catch (e: Exception) {
                finish.take(10)
            }
        } ?: run {
            when (Locale.getDefault().language) {
                "ru" -> "Настоящее время"
                else -> "Present"
            }
        }
    }

    /**
     * ДОПОЛНИТЕЛЬНО: проверяет валидность ISO даты
     */
    fun isValidIsoDate(isoDate: String): Boolean {
        return try {
            Instant.parse(isoDate)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * ДОПОЛНИТЕЛЬНО: получает текущее время в ISO формате
     */
    fun currentTimeIso(): String {
        return Instant.now().toString()
    }
}