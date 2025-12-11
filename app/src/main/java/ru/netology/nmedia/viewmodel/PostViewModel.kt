package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

// Исправленный empty Post
private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = "",
    coords = null,
    link = null,
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    attachment = null
)

private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth,
) : ViewModel() {
    // Используем val data из репозитория
    val data: Flow<PagingData<Post>> = repository.data
        .cachedIn(viewModelScope)

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _prependState = MutableLiveData<FeedModelState>(FeedModelState())
    val prependState: LiveData<FeedModelState>
        get() = _prependState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    // Текущий пользователь для проверки владельца поста
    val currentUserId: Long?
        get() = auth.authStateFlow.value?.id

    init {
        loadPosts()
    }

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
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPrepend() = viewModelScope.launch {
        try {
            _prependState.value = FeedModelState(loading = true, refreshing = true)
            val newPosts = repository.refreshPrepend()
            _prependState.value = FeedModelState(
                refreshPrependCount = newPosts.size
            )
        } catch (e: Exception) {
            _prependState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    // Создаем MediaUpload из фото если есть
                    val mediaUpload = _photo.value?.uri?.let { uri ->
                        val file = File(uri.path ?: return@let null)
                        MediaUpload(file)
                    }

                    // Сохраняем пост
                    repository.save(post, mediaUpload)
                    _postCreated.call()

                    // Сбрасываем состояние
                    edited.value = empty
                    _photo.value = noPhoto

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
                    repository.dislikeById(post.id)
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

    fun isAuthenticated(): Boolean {
        return auth.authStateFlow.value?.token != null
    }
}