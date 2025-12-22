package ru.netology.nework.repository

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import ru.netology.nework.R
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dao.event.EventDao
import ru.netology.nework.dao.post.PostDao
import ru.netology.nework.dao.user.UserDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.entity.event.EventEntity
import ru.netology.nework.entity.post.PostEntity
import ru.netology.nework.entity.user.UserEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.AuthModel
import ru.netology.nework.repository.remotemediator.EventRemoteMediator
import ru.netology.nework.repository.remotemediator.PostRemoteMediator
import ru.netology.nework.repository.remotemediator.UserRemoteMediator
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
class RepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val appAuth: AppAuth,
    private val apiService: ApiService,
    private val postDao: PostDao,
    postRemoteMediator: PostRemoteMediator,
    private val eventDao: EventDao,
    eventRemoteMediator: EventRemoteMediator,
    userDao: UserDao,
    userRemoteMediator: UserRemoteMediator
) : Repository {

    override val dataAuth: StateFlow<AuthModel> = appAuth.authState

    override val dataPost: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 4, enablePlaceholders = false),
        pagingSourceFactory = { postDao.pagingSource() },
        remoteMediator = postRemoteMediator
    ).flow
        .map {
            it.map(PostEntity::toDto)
        }


    override val dataEvent: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 4, enablePlaceholders = false),
        pagingSourceFactory = { eventDao.pagingSource() },
        remoteMediator = eventRemoteMediator
    ).flow
        .map {
            it.map(EventEntity::toDto)
        }

    override val dataUsers: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 4, enablePlaceholders = false),
        pagingSourceFactory = { userDao.pagingSource() },
        remoteMediator = userRemoteMediator
    ).flow.map {
        it.map(UserEntity::toDto)
    }

    private val _dataJob = MutableLiveData<List<Job>>()
    override val dataJob: LiveData<List<Job>> = _dataJob

    override suspend fun register(
        login: String,
        name: String,
        pass: String,
        attachmentModel: AttachmentModel?
    ) {
        try {
            val response = if (attachmentModel != null) {
                val part = MultipartBody.Part.createFormData(
                    "file",
                    attachmentModel.file.name,
                    attachmentModel.file.asRequestBody()
                )
                apiService.usersRegistrationWithPhoto(login, pass, name, part)
            } else {
                apiService.usersRegistration(login, pass, name)
            }

            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> {
                        toastMsg(context.getString(R.string.the_user_is_already_registered))
                    }

                    415 -> {
                        toastMsg(context.getString(R.string.incorrect_photo_format))
                    }

                    else -> throw ApiError(response.code(), response.message())
                }
                return
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun login(login: String, pass: String) {
        try {
            val response = apiService.usersAuthentication(login, pass)
            if (!response.isSuccessful) {
                when (response.code()) {
                    400 -> {
                        toastMsg(context.getString(R.string.incorrect_password))
                    }

                    404 -> {
                        toastMsg(context.getString(R.string.user_unregistered))
                    }

                    else -> throw ApiError(response.code(), response.message())
                }
                return
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun logout() {
        appAuth.removeAuth()
    }

    override suspend fun getUser(id: Long): UserResponse {
        try {
            val response = apiService.usersGetUser(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun like(post: Post) {
        try {
            val response = when (post.likedByMe) {
                true -> {
                    apiService.postsUnLikePost(post.id)
                }

                else -> {
                    apiService.postsLikePost(post.id)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun savePost(post: Post) {
        try {
            val response = apiService.postsSavePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun savePostWithAttachment(post: Post, attachmentModel: AttachmentModel) {
        try {
            val mediaResponse = saveMedia(attachmentModel.file)
            if (!mediaResponse.isSuccessful) {
                throw ApiError(mediaResponse.code(), mediaResponse.message())
            }
            val media = mediaResponse.body() ?: throw ApiError(
                mediaResponse.code(),
                mediaResponse.message()
            )

            val response = apiService.postsSavePost(
                post.copy(
                    attachment = Attachment(
                        media.url,
                        attachmentModel.attachmentType
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveMedia(file: File): Response<Media> {
        val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return apiService.mediaSaveMedia(part)
    }

    override suspend fun deletePost(id: Long) {
        try {
            val response = apiService.postsDeletePost(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            postDao.deletePost(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val response = apiService.eventsSaveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, attachmentModel: AttachmentModel) {
        try {
            val mediaResponse = saveMedia(attachmentModel.file)
            if (!mediaResponse.isSuccessful) {
                throw ApiError(mediaResponse.code(), mediaResponse.message())
            }
            val media = mediaResponse.body() ?: throw ApiError(
                mediaResponse.code(),
                mediaResponse.message()
            )

            val response = apiService.eventsSaveEvent(
                event.copy(
                    attachment = Attachment(
                        media.url,
                        attachmentModel.attachmentType
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun deleteEvent(id: Long) {
        try {
            val response = apiService.eventsDeleteEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            eventDao.deleteEvent(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeEvent(event: Event) {
        try {
            val response = when (event.likedByMe) {
                true -> {
                    apiService.eventsUnLikeEvent(event.id)
                }

                else -> {
                    apiService.eventsLikeEvent(event.id)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getMyJobs() {
        try {
            val response = apiService.myJobGetAllJob()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            _dataJob.value = body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getJobs(userId: Long) {
        try {
            val response = apiService.jobsGetAllJob(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            _dataJob.value = body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val response = apiService.myJobSaveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())

            getMyJobs()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun deleteJob(id: Long) {
        try {
            val response = apiService.myJobDeleteJob(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            _dataJob.value = _dataJob.value?.filter { it.id != id }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    private fun toastMsg(msg: String) {
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }

}