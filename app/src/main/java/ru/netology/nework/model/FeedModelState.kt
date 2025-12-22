package ru.netology.nework.model

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val refreshPrependCount: Int = 5,  // Количество новых постов, добавленных сверху
    val prependLoading: Boolean = false,  // Загрузка для ручного prepend




)
