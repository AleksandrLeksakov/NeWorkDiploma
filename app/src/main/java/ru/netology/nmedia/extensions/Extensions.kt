package ru.netology.nmedia.extensions

import ru.netology.nmedia.dto.*
import ru.netology.nmedia.util.DateUtils

// ============ POST EXTENSIONS ============

/**
 * Форматирует дату публикации поста согласно ТЗ: "dd.mm.yyyy HH:mm"
 */
fun Post.formatPublishedDate(): String = DateUtils.formatPostDate(published)

/**
 * Получает текст для отображения количества лайков
 * Использует likeOwnerIds.size (вычисляемое свойство likes)
 */
fun Post.getLikeText(): String = when (likes) {
    0 -> ""
    1 -> "1 like"
    else -> "$likes likes"
}

/**
 * Проверяет, есть ли у поста вложение
 */
fun Post.hasAttachment(): Boolean = attachment != null

/**
 * Получает текст с упомянутыми пользователями
 */
fun Post.getMentionsText(): String {
    return if (mentionIds.isNotEmpty()) {
        "Упомянуто пользователей: ${mentionIds.size}"
    } else {
        ""
    }
}

// ============ EVENT EXTENSIONS ============

/**
 * Форматирует дату публикации события согласно ТЗ: "dd.mm.yyyy HH:mm"
 */
fun Event.formatPublishedDate(): String = DateUtils.formatPostDate(published)

/**
 * Форматирует дату проведения события согласно ТЗ: "dd.mm.yyyy HH:mm"
 */
fun Event.formatEventDate(): String = DateUtils.formatPostDate(datetime)

/**
 * Получает текст для отображения количества лайков события
 */
fun Event.getLikeText(): String = when (likes) {
    0 -> ""
    1 -> "1 like"
    else -> "$likes likes"
}

/**
 * Получает текст для отображения участников события
 */
fun Event.getParticipantsText(): String = when (participants) {
    0 -> "Нет участников"
    1 -> "1 участник"
    in 2..4 -> "$participants участника"
    else -> "$participants участников"
}

/**
 * Получает текст для отображения спикеров события
 */
fun Event.getSpeakersText(): String = when (speakers) {
    0 -> "Нет спикеров"
    1 -> "1 спикер"
    in 2..4 -> "$speakers спикера"
    else -> "$speakers спикеров"
}

/**
 * Получает тип события для отображения
 */
fun Event.getTypeDisplay(): String = when (type) {
    "ONLINE" -> "Онлайн"
    "OFFLINE" -> "Офлайн"
    else -> type
}

// ============ JOB EXTENSIONS ============

/**
 * Форматирует период работы согласно ТЗ: "dd MMM yyyy"
 * Пример: "08 Dec 2024 - настоящее время"
 */
fun Job.formatPeriod(): String = DateUtils.formatJobPeriod(this)

/**
 * Получает отображаемый текст для карточки работы
 * Пример: "Android Developer в Яндекс"
 */
fun Job.getDisplayText(): String = "$position в $name"

/**
 * Получает полный текст для отображения работы
 * Пример: "Android Developer в Яндекс\n08 Dec 2024 - настоящее время"
 */
fun Job.getFullDisplayText(): String =
    "$position в $name\n${formatPeriod()}"

/**
 * Проверяет, является ли работа текущей
 */
fun Job.isCurrent(): Boolean = finish == null || finish.isBlank()

// ============ USER EXTENSIONS ============

/**
 * Получает отображаемое имя пользователя
 * Использует имя, если оно есть, иначе логин
 */
fun UserResponse.getDisplayName(): String = name.takeIf { it.isNotBlank() } ?: login

/**
 * Получает инициалы для аватара
 */
fun UserResponse.getInitials(): String {
    return if (name.isNotBlank()) {
        name.split(" ")
            .take(2)
            .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
            .take(2)
    } else {
        login.take(2).uppercase()
    }
}

/**
 * Проверяет, есть ли у пользователя аватар
 */
fun UserResponse.hasAvatar(): Boolean = !avatar.isNullOrBlank()

// ============ COMMENT EXTENSIONS ============

fun Comment.formatPublishedDate(): String = DateUtils.formatPostDate(published)
fun Comment.getLikeText(): String = when (likes) {
    0 -> ""
    1 -> "1 like"
    else -> "$likes likes"
}

// ============ STRING EXTENSIONS ============

/**
 * Сокращает длинный текст с добавлением "..."
 */
fun String.truncate(maxLength: Int = 100): String {
    return if (length > maxLength) {
        substring(0, maxLength) + "..."
    } else {
        this
    }
}

/**
 * Проверяет, является ли строка валидным URL
 */
fun String.isValidUrl(): Boolean {
    return startsWith("http://") || startsWith("https://")
}

/**
 * Извлекает домен из URL для отображения
 */
fun String.extractDomain(): String {
    return try {
        val url = java.net.URL(this)
        url.host
    } catch (e: Exception) {
        this
    }
}