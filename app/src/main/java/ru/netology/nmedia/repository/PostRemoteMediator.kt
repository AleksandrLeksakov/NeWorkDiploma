package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
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
                    remoteKeys.forEach { key ->
                        postRemoteKeyDao.insert(key)  // Вставляем каждый ключ отдельно
                    }
                }

                // Сохраняем посты (предполагаем что у Post есть метод toEntity())
                val postEntities = posts.map { post ->
                    // Преобразуем Post в PostEntity
                    PostEntity(
                        id = post.id,
                        authorId = post.authorId,
                        author = post.author,
                        authorJob = post.authorJob,
                        authorAvatar = post.authorAvatar,
                        content = post.content,
                        published = post.published,
                        coordinates = post.coordinates?.let {
                            ru.netology.nmedia.entity.CoordinatesEmbeddable(
                                lat = it.lat.toString(),
                                long = it.long.toString()
                            )
                        },
                        link = post.link,
                        mentionIds = post.mentionIds,
                        mentionedMe = post.mentionedMe,
                        likeOwnerIds = post.likeOwnerIds,
                        likedByMe = post.likedByMe,
                        attachment = post.attachment?.let {
                            ru.netology.nmedia.entity.AttachmentEmbeddable(
                                url = it.url,
                                type = it.type
                            )
                        }
                    )
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