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
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {

    private var isInitialLoad = true

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    if (isInitialLoad) {
                        service.getLatest(state.config.initialLoadSize)
                    } else {
                        val maxId = postRemoteKeyDao.max()
                        if (maxId != null) {
                            service.getAfter(maxId, state.config.pageSize)
                        } else {
                            service.getLatest(state.config.initialLoadSize)
                        }
                    }
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body: List<Post> = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            if (body.isEmpty()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (isInitialLoad) {
                            postRemoteKeyDao.removeAll()
                            postDao.removeAll()
                        }

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            )
                        )

                        if (isInitialLoad) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    id = body.last().id,
                                )
                            )
                        }

                        if (isInitialLoad) {
                            isInitialLoad = false
                        }
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }

                    else -> { /* Do nothing for PREPEND */ }
                }

                // Используем правильное преобразование
                val posts = body.map { post -> PostEntity.fromDto(post) }
                postDao.insert(posts)
            }

            return MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            return MediatorResult.Error(e)
        }
    }

    suspend fun refreshPrepend(): List<PostEntity> {
        return try {
            val maxId = postRemoteKeyDao.max() ?: return emptyList()
            val response = service.getAfter(maxId, 10)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body: List<Post> = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            if (body.isEmpty()) {
                return emptyList()
            }

            db.withTransaction {
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        type = PostRemoteKeyEntity.KeyType.AFTER,
                        id = body.first().id,
                    )
                )

                val posts = body.map { post -> PostEntity.fromDto(post) }
                postDao.insert(posts)
            }

            body.map { post -> PostEntity.fromDto(post) }
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            emptyList()
        }
    }
}