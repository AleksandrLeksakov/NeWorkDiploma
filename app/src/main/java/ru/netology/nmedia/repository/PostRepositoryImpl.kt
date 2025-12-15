package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val api: ApiService,
) : PostRepository {

    // Временные данные для тестирования
    private val testPosts = listOf(
        Post(
            id = 1,
            authorId = 1,
            author = "Иван Иванов",
            authorJob = "Android разработчик",
            authorAvatar = "https://via.placeholder.com/150",
            content = "Привет! Это мой первый пост в NeWork!",
            published = "2024-01-15T10:30:00Z",
            coordinates = null,
            link = null,
            mentionIds = emptyList(),
            mentionedMe = false,
            likeOwnerIds = listOf(2, 3),
            likedByMe = false,
            attachment = null
        ),
        Post(
            id = 2,
            authorId = 2,
            author = "Мария Петрова",
            authorJob = "Дизайнер",
            authorAvatar = "https://via.placeholder.com/150",
            content = "Отличная погода сегодня!",
            published = "2024-01-14T15:45:00Z",
            coordinates = null,
            link = null,
            mentionIds = listOf(1),
            mentionedMe = true,
            likeOwnerIds = listOf(1, 3),
            likedByMe = true,
            attachment = null
        )
    )

    override val data: Flow<List<Post>> = flow {
        emit(testPosts) // Временно возвращаем тестовые данные
    }

    override suspend fun getAll() {
        try {
            val response = api.getAll()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    dao.insert(body.map { PostEntity.fromDto(it) })
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
                    dao.insert(PostEntity.fromDto(body))
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
                    dao.insert(PostEntity.fromDto(body))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                    dao.insert(PostEntity.fromDto(body))
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
                "file",
                file.name,
                requestFile
            )

            val response = api.upload(body)
            if (response.isSuccessful) {
                return response.body() ?: throw RuntimeException("Media body is null")
            } else {
                throw RuntimeException("Upload failed: ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        emit(0)
    }
}