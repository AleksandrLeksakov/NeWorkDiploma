package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nework.api.ApiService
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val api: ApiService,
    private val db: AppDb
) : RemoteMediator<Int, PostEntity>() {

    private val postDao = db.postDao()
    private val postRemoteKeyDao = db.postRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        return try {
            // Определяем ключ для загрузки
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    // Получаем ключ из БД
                    val remoteKey = db.withTransaction {
                        postRemoteKeyDao.keyById(lastItem.id)
                    }

                    // Если ключа нет или nextKey null - конец пагинации
                    remoteKey?.nextKey ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                }
            }

            // Загружаем данные с API
            val response = if (loadKey == null) {
                // Первая загрузка или обновление
                api.getLatest(state.config.pageSize)
            } else {
                // Продолжение пагинации
                api.getAfter(loadKey, state.config.pageSize)
            }

            // Проверяем ответ
            if (!response.isSuccessful) {
                return MediatorResult.Error(IOException("Failed to load: ${response.code()}"))
            }

            val posts = response.body() ?: emptyList()
            val endOfPaginationReached = posts.isEmpty()

            // Сохраняем в БД в транзакции
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    postDao.removeAll()
                    postRemoteKeyDao.clear()
                }

                // Сохраняем ключи для следующей загрузки
                if (posts.isNotEmpty()) {
                    val remoteKeys = posts.map { post ->
                        PostRemoteKeyEntity(
                            id = post.id,
                            nextKey = posts.lastOrNull()?.id?.takeIf { it != post.id }
                        )
                    }
                    postRemoteKeyDao.insert(remoteKeys)
                }

                // Сохраняем посты используя метод fromDto
                val postEntities = posts.map { post ->
                    PostEntity.fromDto(post)  // Используем метод fromDto
                }
                postDao.insert(postEntities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}