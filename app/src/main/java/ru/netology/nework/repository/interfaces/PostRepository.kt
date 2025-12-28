package ru.netology.nework.repository.interfaces

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.model.AttachmentModel

interface PostRepository {

    val dataPost: Flow<PagingData<FeedItem>>

    suspend fun like(post: Post)

    suspend fun savePost(post: Post)

    suspend fun savePostWithAttachment(post: Post, attachmentModel: AttachmentModel)

    suspend fun deletePost(id: Long)
}