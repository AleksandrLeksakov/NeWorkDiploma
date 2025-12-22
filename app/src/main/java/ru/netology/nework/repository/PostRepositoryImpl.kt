package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.post.PostDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.PostEntity
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val api: ApiService,
    private val db: AppDb
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        remoteMediator = PostRemoteMediator(api, db),
        pagingSourceFactory = { dao.pagingSource() }
    ).flow.map { pagingData ->
        pagingData.map { it.toDto() }  // Преобразуем PostEntity в Post
    }

    // Остальные методы остаются, но исправим преобразование coordinates
    override suspend fun getAll() {
        try {
            val response = api.getLatest(20)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val entities = body.map { post ->
                        PostEntity.fromDto(post)  // Используем метод fromDto
                    }
                    dao.insert(entities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = api.likeById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val entity = PostEntity.fromDto(body)  // Используем метод fromDto
                    dao.insert(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun unlikeById(id: Long) {
        try {
            val response = api.unlikeById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val entity = PostEntity.fromDto(body)  // Используем метод fromDto
                    dao.insert(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getById(id: Long): Post? {
        return try {
            val response = api.getPostById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = api.removeById(id)
            if (response.isSuccessful) {
                dao.removeById(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = api.save(post)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val entity = PostEntity.fromDto(body)  // Используем метод fromDto
                    dao.insert(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val file = upload.file
            val requestFile = file.asRequestBody("image/*".toMediaType())
            val body = MultipartBody.Part.createFormData(
                "file",  // Имя поля должно соответствовать API
                file.name,
                requestFile
            )

            val response = api.upload(body)
            if (response.isSuccessful) {
                val media = response.body()
                if (media != null) {
                    return media
                } else {
                    throw RuntimeException("Media body is null")
                }
            } else {
                throw RuntimeException("Upload failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getNewerCount(id: Long): Int {
        return dao.getNewerCount(id)
    }
}