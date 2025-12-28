package ru.netology.nework.adapter.listeners

/**
 * Интерфейс для взаимодействия с событиями
 */
interface EventInteractionListener :
    LikeListener,
    DeleteListener,
    EditListener,
    CardOpenListener