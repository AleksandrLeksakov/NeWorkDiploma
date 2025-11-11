package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {

    // Добавляем флаг для отслеживания первого запуска
    private var isInitialLoad = true

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    // Флаг НЕ сбрасываем здесь!
                    if (isInitialLoad) {
                        // При первом запуске загружаем начальные данные
                        service.getLatest(state.config.initialLoadSize)
                    } else {
                        // При последующих refresh - загружаем только новые данные
                        val maxId = postRemoteKeyDao.max()
                        if (maxId != null) {
                            service.getAfter(maxId, state.config.pageSize)
                        } else {
                            service.getLatest(state.config.initialLoadSize)
                        }
                    }
                }

                LoadType.PREPEND -> {
                    // ОТКЛЮЧАЕМ автоматический PREPEND
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    // APPEND работает в обычном режиме
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            if (body.isEmpty()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        // Все проверки ДО сброса флага
                        if (isInitialLoad) {
                            // При первом запуске очищаем всё
                            postRemoteKeyDao.removeAll()
                            postDao.removeAll()
                        }
                        // При последующих REFRESH НЕ очищаем ключи и БД

                        // Всегда вставляем AFTER ключ
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            )
                        )

                        //BEFORE ключ обновляем только если БД пуста
                        // Проверяем ДО сброса флага
                        if (isInitialLoad) { // <---
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    id = body.last().id,
                                )
                            )
                        }


                        //  Сбрасываем флаг ПОСЛЕ всех проверок
                        if (isInitialLoad) {
                            isInitialLoad = false
                        }
                    }

                    LoadType.PREPEND -> {
                        // PREPEND отключен - ничего не делаем
                    }

                    LoadType.APPEND -> {
                        // Обновляем ключ для APPEND
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }

                // Вставляем новые посты (БД сама обработает конфликты через UNIQUE)
                postDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            return MediatorResult.Error(e)
        }
    }

    // Новый метод для ручного добавления данных сверху (refresh to prepend)
    suspend fun refreshPrepend(): List<PostEntity> {
        return try {
            val maxId = postRemoteKeyDao.max() ?: return emptyList()
            val response = service.getAfter(maxId, 10) // Загружаем новые посты после текущего максимума

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            if (body.isEmpty()) {
                return emptyList()
            }

            db.withTransaction {
                // Обновляем AFTER ключ
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        type = PostRemoteKeyEntity.KeyType.AFTER,
                        id = body.first().id,
                    )
                )

                // Вставляем новые посты
                postDao.insert(body.toEntity())
            }

            body.toEntity()
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            emptyList()
        }
    }
}