package ru.netology.nework.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.AuthModel

interface Repository {
    val dataAuth: StateFlow<AuthModel>
    val dataPost: Flow<PagingData<FeedItem>>
    val dataEvent: Flow<PagingData<FeedItem>>
    val dataUsers: Flow<PagingData<FeedItem>>
    val dataJob: LiveData<List<Job>>
    suspend fun register(
        login: String,
        name: String,
        pass: String,
        attachmentModel: AttachmentModel?
    )

    suspend fun login(login: String, pass: String)
    fun logout()

    suspend fun getUser(id: Long): UserResponse
    suspend fun like(post: Post)
    suspend fun savePost(post: Post)
    suspend fun savePostWithAttachment(post: Post, attachmentModel: AttachmentModel)
    suspend fun deletePost(id: Long)

    suspend fun saveEvent(event: Event)
    suspend fun saveEventWithAttachment(event: Event, attachmentModel: AttachmentModel)
    suspend fun deleteEvent(id: Long)
    suspend fun likeEvent(event: Event)

    suspend fun getMyJobs()
    suspend fun getJobs(userId: Long)
    suspend fun saveJob(job: Job)
    suspend fun deleteJob(id: Long)
}