package ru.netology.nework.adapter.listeners

/**
 * Интерфейс для взаимодействия с постами
 */
interface PostInteractionListener :
    LikeListener,
    DeleteListener,
    EditListener,
    CardOpenListener