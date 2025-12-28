package ru.netology.nework.repository.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.services.PostApiService
import ru.netology.nework.dao.post.PostDao
import ru.netology.nework.dao.post.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.keytype.KeyType
import ru.netology.nework.entity.post.PostEntity
import ru.netology.nework.entity.post.PostRemoteKeyEntity
import ru.netology.nework.entity.post.toEntity
import ru.netology.nework.error.ApiError
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class PostRemoteMediator @Inject constructor(
    private val postApiService: PostApiService,
    private val appDb: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun initialize(): InitializeAction =
        if (postDao.isEmpty()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {

        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id != null) {
                        postApiService.getAfter(id, state.config.pageSize)
                    } else {
                        postApiService.getLatest(state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    postApiService.getAfter(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    postApiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body()
                ?: throw ApiError(response.code(), response.message())

            if (body.isNotEmpty()) {
                appDb.withTransaction {
                    when (loadType) {
                        LoadType.REFRESH -> {
                            if (postDao.isEmpty()) {
                                postRemoteKeyDao.insert(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            KeyType.AFTER,
                                            body.first().id
                                        ),
                                        PostRemoteKeyEntity(
                                            KeyType.BEFORE,
                                            body.last().id
                                        )
                                    )
                                )
                            } else {
                                postRemoteKeyDao.insert(
                                    PostRemoteKeyEntity(
                                        KeyType.AFTER,
                                        body.first().id
                                    )
                                )
                            }
                        }

                        LoadType.PREPEND -> {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }

                        LoadType.APPEND -> {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }

                    postDao.insertAll(body.toEntity())
                }
            }

            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}