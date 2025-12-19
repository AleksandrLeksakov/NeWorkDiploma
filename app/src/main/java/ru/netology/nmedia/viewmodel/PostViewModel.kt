package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = "",
    coordinates = null,
    link = null,
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    attachment = null
)

private val noPhoto = PhotoModel()

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    // ПРОСТОЙ ВАРИАНТ - repository.data уже возвращает Flow<PagingData<Post>>
    val data: Flow<PagingData<Post>> = repository.data
        .cachedIn(viewModelScope)

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()  // Просто перезагружаем все
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    // Обработка медиа
                    val mediaUpload = _photo.value?.uri?.let { uri ->
                        val file = File(uri.path ?: return@let null)
                        MediaUpload(file)
                    }

                    // Если есть медиа, сначала загружаем его
                    var attachmentUrl: String? = null
                    mediaUpload?.let {
                        val media = repository.upload(it)
                        attachmentUrl = media.url
                    }

                    // Создаем пост с вложением
                    val postToSave = if (attachmentUrl != null) {
                        post.copy(
                            attachment = ru.netology.nmedia.dto.Attachment(
                                url = attachmentUrl!!,
                                type = ru.netology.nmedia.enumeration.AttachmentType.IMAGE
                            )
                        )
                    } else {
                        post
                    }

                    repository.save(postToSave)

                    edited.value = empty
                    _photo.value = noPhoto
                    _postCreated.value = Unit

                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                if (post.likedByMe) {
                    repository.unlikeById(post.id)
                } else {
                    repository.likeById(post.id)
                }
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun createNewPost(content: String) {
        val currentTime = java.time.Instant.now().toString()
        val newPost = empty.copy(
            content = content,
            published = currentTime
        )
        edited.value = newPost
    }
}