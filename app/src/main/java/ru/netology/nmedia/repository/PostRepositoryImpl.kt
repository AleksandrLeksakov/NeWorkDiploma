package ru.netology.nmedia.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val api: ApiService,
    private val db: AppDb
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<PostEntity>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        remoteMediator = PostRemoteMediator(api, db),
        pagingSourceFactory = { dao.pagingSource() }
    ).flow  // УБИРАЕМ .map преобразование!

    override suspend fun getAll() {
        try {
            val response = api.getLatest(20)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val entities = body.map { post ->
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
                    val entity = PostEntity(
                        id = body.id,
                        authorId = body.authorId,
                        author = body.author,
                        authorJob = body.authorJob,
                        authorAvatar = body.authorAvatar,
                        content = body.content,
                        published = body.published,
                        coordinates = body.coordinates?.let {
                            ru.netology.nmedia.entity.CoordinatesEmbeddable(
                                lat = it.lat.toString(),
                                long = it.long.toString()
                            )
                        },
                        link = body.link,
                        mentionIds = body.mentionIds,
                        mentionedMe = body.mentionedMe,
                        likeOwnerIds = body.likeOwnerIds,
                        likedByMe = body.likedByMe,
                        attachment = body.attachment?.let {
                            ru.netology.nmedia.entity.AttachmentEmbeddable(
                                url = it.url,
                                type = it.type
                            )
                        }
                    )
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
                    val entity = PostEntity(
                        id = body.id,
                        authorId = body.authorId,
                        author = body.author,
                        authorJob = body.authorJob,
                        authorAvatar = body.authorAvatar,
                        content = body.content,
                        published = body.published,
                        coordinates = body.coordinates?.let {
                            ru.netology.nmedia.entity.CoordinatesEmbeddable(
                                lat = it.lat.toString(),
                                long = it.long.toString()
                            )
                        },
                        link = body.link,
                        mentionIds = body.mentionIds,
                        mentionedMe = body.mentionedMe,
                        likeOwnerIds = body.likeOwnerIds,
                        likedByMe = body.likedByMe,
                        attachment = body.attachment?.let {
                            ru.netology.nmedia.entity.AttachmentEmbeddable(
                                url = it.url,
                                type = it.type
                            )
                        }
                    )
                    dao.insert(entity)
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
                    val entity = PostEntity(
                        id = body.id,
                        authorId = body.authorId,
                        author = body.author,
                        authorJob = body.authorJob,
                        authorAvatar = body.authorAvatar,
                        content = body.content,
                        published = body.published,
                        coordinates = body.coordinates?.let {
                            ru.netology.nmedia.entity.CoordinatesEmbeddable(
                                lat = it.lat.toString(),
                                long = it.long.toString()
                            )
                        },
                        link = body.link,
                        mentionIds = body.mentionIds,
                        mentionedMe = body.mentionedMe,
                        likeOwnerIds = body.likeOwnerIds,
                        likedByMe = body.likedByMe,
                        attachment = body.attachment?.let {
                            ru.netology.nmedia.entity.AttachmentEmbeddable(
                                url = it.url,
                                type = it.type
                            )
                        }
                    )
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

    override suspend fun getNewerCount(id: Long): Int {
        return dao.getNewerCount(id)
    }
}