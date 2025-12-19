package ru.netology.nmedia.extensions

import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Для ошибок в EditText - явное приведение типа
fun EditText.setErrorText(text: String?) {
    val charSequence: CharSequence? = text
    error = charSequence
}

// Для ошибок в TextInputLayout (Material Design) - явное приведение типа
fun TextInputLayout.setMaterialErrorText(text: String?) {
    val charSequence: CharSequence? = text
    error = charSequence
}

// Форматирование даты
fun formatPublishedDate(published: String): String {
    return try {
        val instant = Instant.parse(published)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
        dateTime.format(formatter)
    } catch (e: Exception) {
        published
    }
}

// Форматирование текста лайков для MaterialButton
fun MaterialButton.setLikeText(count: Int) {
    text = formatLikeCount(count)
}

// Вспомогательная функция для форматирования количества лайков
private fun formatLikeCount(count: Int): String {
    val likeCount = when (count) {
        0 -> ""
        1 -> "1"
        2, 3, 4 -> "$count"
        else -> "$count"
    }

    val suffix = when {
        count % 10 == 1 && count % 100 != 11 -> "лайк"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "лайка"
        else -> "лайков"
    }

    return if (likeCount.isNotEmpty()) "$likeCount $suffix" else "Нравится"
}