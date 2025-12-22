package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.dto.Post

interface PostRepository {
    val data: Flow<PagingData<Post>>

    suspend fun getAll()
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun getNewerCount(id: Long): Int

    suspend fun getById(id: Long): Post?
}