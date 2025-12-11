package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post

interface PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    val data: Flow<PagingData<Post>> // Исправьте на val

    suspend fun getAll()
    suspend fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post, upload: MediaUpload?) // Два параметра
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun dislikeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun refreshPrepend(): List<Post>
}