package ru.netology.nework.repository

import ru.netology.nework.repository.interfaces.*

/**
 * Главный интерфейс репозитория, объединяющий все специализированные интерфейсы.
 * Используется для обратной совместимости и удобства внедрения зависимостей.
 */
interface Repository :
    AuthRepository,
    UserRepository,
    PostRepository,
    EventRepository,
    JobRepository,
    WallRepository