package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.InvolvedItemModel
import ru.netology.nework.model.InvolvedItemType
import ru.netology.nework.repository.Repository
import java.io.File
import java.time.OffsetDateTime
import javax.inject.Inject

val emptyPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = OffsetDateTime.now(),
    coords = null,
    link = null,
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    attachment = null,
    users = mapOf(),
    ownedByMe = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: Repository,
    appAuth: AppAuth
) : ViewModel() {

    val data: Flow<PagingData<FeedItem>> = appAuth.authState
        .flatMapLatest { auth ->
            repository.dataPost.map {
                it.map { feedItem ->
                    if (feedItem is Post) {
                        feedItem.copy(
                            ownedByMe = auth.id == feedItem.authorId,
                            likedByMe = !feedItem.likeOwnerIds.none { id ->
                                id == auth.id
                            }
                        )
                    } else {
                        feedItem
                    }
                }
            }
        }.flowOn(Dispatchers.Default)


    private val _editedPost = MutableLiveData(emptyPost)
    val editedPost: LiveData<Post> = _editedPost

    val postData = MutableLiveData<Post>()

    val involvedData = MutableLiveData(InvolvedItemModel())

    private val _attachmentData: MutableLiveData<AttachmentModel?> = MutableLiveData(null)
    val attachmentData: LiveData<AttachmentModel?>
        get() = _attachmentData


    fun savePost(content: String) {
        val text = content.trim()
        if (_editedPost.value?.content == text) {
            _editedPost.value = emptyPost
            return
        }
        _editedPost.value = _editedPost.value?.copy(content = text)
        _editedPost.value?.let {
            viewModelScope.launch {
                val attachment = _attachmentData.value
                if (attachment == null) {
                    repository.savePost(
                        it
                    )
                } else {
                    repository.savePostWithAttachment(
                        it, attachment
                    )
                }
            }
        }
        _editedPost.value = emptyPost
        _attachmentData.value = null
    }

    fun deletePost(post: Post) = viewModelScope.launch {
        repository.deletePost(post.id)
    }

    fun like(post: Post) = viewModelScope.launch {
        try {
            repository.like(post)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun edit(post: Post) {
        _editedPost.value = post
    }

    fun setAttachment(uri: Uri, file: File, attachmentType: AttachmentType) {
        _attachmentData.value = AttachmentModel(attachmentType, uri, file)
    }

    fun removePhoto() {
        _attachmentData.value = null
    }

    fun setCoord(point: Point?) {
        if (point != null) {
            _editedPost.value = _editedPost.value?.copy(
                coords = Coordinates(point.latitude, point.longitude)
            )
        }
    }

    fun removeCoords() {
        _editedPost.value = _editedPost.value?.copy(
            coords = null
        )
    }

    fun setMentionId(selectedUsers: List<Long>) {
        _editedPost.value = _editedPost.value?.copy(
            mentionIds = selectedUsers
        )
    }

    fun openPost(post: Post) {
        postData.value = post
    }


    suspend fun getInvolved(involved: List<Long>, involvedItemType: InvolvedItemType) {
        val list = involved
            .let {
                if (it.size > 4) it.take(5) else it
            }
            .map {
                viewModelScope.async { repository.getUser(it) }
            }.awaitAll()

        synchronized(involvedData) {
            when (involvedItemType) {

                InvolvedItemType.LIKERS -> {
                    involvedData.value = involvedData.value?.copy(
                        likers = list
                    )
                }

                InvolvedItemType.MENTIONED -> {
                    involvedData.value = involvedData.value?.copy(
                        mentioned = list
                    )
                }

                else -> return
            }
        }

    }

    fun resetInvolved() {
        involvedData.value = InvolvedItemModel()
    }

}