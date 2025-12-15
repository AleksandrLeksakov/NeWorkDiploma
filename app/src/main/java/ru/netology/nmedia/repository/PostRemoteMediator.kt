package ru.netology.nmedia.repository

/*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val auth: AppAuth,
) : RemoteMediator<Int, PostEntity>() {

    private val postDao = db.postDao()
    private val postRemoteKeyDao = db.postRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    service.getLatest(state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    val id = state.lastItemOrNull()?.id ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    val key = postRemoteKeyDao.keyById(id)
                    if (key?.nextKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    service.getAfter(key.nextKey, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                return MediatorResult.Error(IOException(response.message()))
            }

            val body = response.body() ?: return MediatorResult.Success(
                endOfPaginationReached = true
            )

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postRemoteKeyDao.clear()
                        postDao.removeAll()
                    }
                    else -> Unit
                }

                val nextKey = body.lastOrNull()?.id
                postRemoteKeyDao.insert(
                    PostRemoteKeyEntity(
                        type = PostRemoteKeyEntity.Type.POST,
                        id = body.first().id,
                        nextKey = nextKey
                    )
                )
                postDao.insert(body.toEntity())
            }

            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
}
*/